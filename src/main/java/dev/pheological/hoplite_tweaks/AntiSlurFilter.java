package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Locally blocks outgoing Hoplite chat using a remotely maintained literal term list.
 */
public final class AntiSlurFilter {
    private static final int MAX_LIST_BYTES = 256 * 1024;
    private static final int MAX_RULES = 5_000;
    private static final int MAX_RULE_LENGTH = 128;
    /*
     * Modrinth review note: this fixed, first-party URL is used only to download the
     * UTF-8 moderation word list maintained in the Hoplite Tweaks repository. The
     * request is a one-way GET; no chat messages, player identifiers, server data,
     * telemetry, or other user information are uploaded.
     */
    private static final URI LIST_URI = URI.create(
        "https://raw.githubusercontent.com/pheological/hoplite-tweaks/main/blocked-words.txt"
    );
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static final AtomicBoolean REFRESHING = new AtomicBoolean();
    private static volatile RuleSet rules = RuleSet.EMPTY;
    private static volatile String etag = "";

    private AntiSlurFilter() {
    }

    public static void initialize() {
        loadBundledRules();
        loadCache();
        refresh();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> refresh());
        ClientSendMessageEvents.ALLOW_CHAT.register(AntiSlurFilter::allowChatMessage);
        ClientSendMessageEvents.ALLOW_COMMAND.register(AntiSlurFilter::allowCommandMessage);
    }

    public static void refresh() {
        if (!REFRESHING.compareAndSet(false, true)) {
            return;
        }

        HttpRequest.Builder request = HttpRequest.newBuilder(LIST_URI)
            .timeout(Duration.ofSeconds(8))
            .header("Accept", "text/plain")
            .header("User-Agent", "Hoplite-Tweaks/1.0")
            .GET();
        if (!etag.isEmpty()) {
            request.header("If-None-Match", etag);
        }

        HTTP.sendAsync(request.build(), HttpResponse.BodyHandlers.ofByteArray())
            .whenComplete((response, error) -> {
                REFRESHING.set(false);
                if (error != null) {
                    HopliteTweaks.LOGGER.warn("Could not refresh anti-slur list; using cached rules");
                    return;
                }
                if (response.statusCode() == 304) {
                    return;
                }
                byte[] body = response.body();
                if (response.statusCode() != 200 || body.length == 0 || body.length > MAX_LIST_BYTES) {
                    HopliteTweaks.LOGGER.warn(
                        "Anti-slur list refresh returned an unusable response ({})",
                        response.statusCode()
                    );
                    return;
                }

                String text = new String(body, StandardCharsets.UTF_8);
                RuleSet parsed = parseRules(text);
                if (parsed.blocked().isEmpty()) {
                    HopliteTweaks.LOGGER.warn("Anti-slur list contains no valid blocked terms; keeping cached rules");
                    return;
                }
                rules = parsed;
                response.headers().firstValue("ETag").ifPresent(value -> etag = value);
                saveCache(text);
                HopliteTweaks.LOGGER.info("Loaded {} anti-slur rules", parsed.blocked().size());
            });
    }

    private static boolean allowChatMessage(String message) {
        return allowOutgoingMessage(message);
    }

    private static boolean allowCommandMessage(String command) {
        return allowOutgoingMessage(command);
    }

    private static boolean allowOutgoingMessage(String message) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (!config.enabled || !config.antiSlur || !HopliteSession.isActive() || !matches(message, rules)) {
            return true;
        }
        HopliteChat.send(
            Component.literal("[Hoplite Tweaks] ")
                .withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal("Message blocked by the anti-slur filter.")
                    .withStyle(ChatFormatting.RED))
        );
        return false;
    }

    private static void loadBundledRules() {
        try (InputStream stream = AntiSlurFilter.class.getResourceAsStream(
            "/assets/hoplite_tweaks/blocked-words.txt"
        )) {
            if (stream == null) {
                HopliteTweaks.LOGGER.warn("Bundled anti-slur list is missing");
                return;
            }
            RuleSet bundled = parseRules(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            if (!bundled.blocked().isEmpty()) {
                rules = bundled;
                HopliteTweaks.LOGGER.info("Loaded {} bundled anti-slur rules", bundled.blocked().size());
            }
        } catch (Exception exception) {
            HopliteTweaks.LOGGER.warn("Could not load the bundled anti-slur list");
        }
    }

    private static void loadCache() {
        try {
            Path cacheFile = cacheFile();
            if (Files.exists(cacheFile)) {
                RuleSet cached = parseRules(Files.readString(cacheFile));
                if (!cached.blocked().isEmpty()) {
                    rules = cached;
                }
            }
        } catch (Exception exception) {
            HopliteTweaks.LOGGER.warn("Could not load the cached anti-slur list");
        }
    }

    private static void saveCache(String text) {
        try {
            Files.createDirectories(cacheDirectory());
            Files.writeString(cacheFile(), text, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            HopliteTweaks.LOGGER.warn("Could not cache the anti-slur list");
        }
    }

    private static Path cacheDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve("hoplite-tweaks");
    }

    private static Path cacheFile() {
        return cacheDirectory().resolve("anti-slur-list.txt");
    }

    static RuleSet parseRules(String text) {
        List<String> blocked = new ArrayList<>();
        List<String> allowed = new ArrayList<>();
        for (String inputLine : text.split("\\R")) {
            String line = inputLine.replace("\uFEFF", "").trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            boolean exception = line.startsWith("!");
            String normalized = normalize(exception ? line.substring(1) : line);
            if (normalized.isEmpty() || normalized.length() > MAX_RULE_LENGTH) {
                continue;
            }
            List<String> target = exception ? allowed : blocked;
            if (!target.contains(normalized)) {
                target.add(normalized);
            }
            if (blocked.size() + allowed.size() >= MAX_RULES) {
                break;
            }
        }
        return new RuleSet(List.copyOf(blocked), List.copyOf(allowed));
    }

    static boolean matches(String message, RuleSet ruleSet) {
        String padded = " " + normalize(message) + " ";
        for (String exception : ruleSet.allowed()) {
            padded = padded.replace(" " + exception + " ", " ");
        }
        for (String blocked : ruleSet.blocked()) {
            if (padded.contains(" " + blocked + " ")) {
                return true;
            }
        }
        return false;
    }

    static String normalize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT);
        StringBuilder output = new StringBuilder(normalized.length());
        boolean previousSpace = true;
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            char replacement = switch (character) {
                case '0' -> 'o';
                case '1', '!' -> 'i';
                case '3' -> 'e';
                case '4', '@' -> 'a';
                case '5', '$' -> 's';
                case '7' -> 't';
                default -> character;
            };
            if (Character.isLetterOrDigit(replacement)) {
                output.append(replacement);
                previousSpace = false;
            } else if (!previousSpace) {
                output.append(' ');
                previousSpace = true;
            }
        }
        return output.toString().trim();
    }

    record RuleSet(List<String> blocked, List<String> allowed) {
        private static final RuleSet EMPTY = new RuleSet(List.of(), List.of());
    }
}
