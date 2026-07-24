package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Applies a configurable style to selected player names in incoming Hoplite chat.
 */
public final class ChatNameHighlighter {
    private static final int MAX_LIST_BYTES = 64 * 1024;
    private static final int MAX_PLAYERS = 1_000;
    private static final Pattern VALID_USERNAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    /*
     * Modrinth review note: this fixed, first-party URL only downloads the public
     * username list maintained in the Hoplite Tweaks repository. The request is a
     * one-way GET and never uploads chat, usernames, server data, or telemetry.
     */
    private static final URI LIST_URI = URI.create(
        "https://raw.githubusercontent.com/pheological/hoplite-tweaks/main/highlighted-players.txt"
    );
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static final AtomicBoolean REFRESHING = new AtomicBoolean();
    private static volatile List<PlayerHighlight> players = List.of();
    private static volatile String etag = "";

    private ChatNameHighlighter() {
    }

    public static void initialize() {
        loadBundledPlayers();
        loadCache();
        refresh();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> refresh());
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) ->
            overlay ? message : highlightIfEnabled(message)
        );
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signed, sender, params, receivedAt) -> {
            Component highlighted = highlightIfEnabled(message);
            if (highlighted == message) {
                return true;
            }
            // Fabric has no MODIFY_CHAT event. Its API directs modifiers to cancel
            // the original and add the styled component to the chat HUD themselves.
            HopliteChat.send(highlighted);
            return false;
        });
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
                    HopliteTweaks.LOGGER.warn(
                        "Could not refresh highlighted player list; using cached names"
                    );
                    return;
                }
                if (response.statusCode() == 304) {
                    return;
                }
                byte[] body = response.body();
                if (response.statusCode() != 200 || body.length == 0 || body.length > MAX_LIST_BYTES) {
                    HopliteTweaks.LOGGER.warn(
                        "Highlighted player list returned an unusable response ({})",
                        response.statusCode()
                    );
                    return;
                }

                String text = new String(body, StandardCharsets.UTF_8);
                List<PlayerHighlight> parsed = parsePlayers(text);
                if (parsed.isEmpty()) {
                    HopliteTweaks.LOGGER.warn(
                        "Highlighted player list contains no valid usernames; keeping cached names"
                    );
                    return;
                }
                players = parsed;
                response.headers().firstValue("ETag").ifPresent(value -> etag = value);
                saveCache(text);
                HopliteTweaks.LOGGER.info("Loaded {} highlighted player names", parsed.size());
            });
    }

    private static Component highlightIfEnabled(Component message) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (!config.enabled || !config.chatNameHighlights || !HopliteSession.isActive()) {
            return message;
        }
        return highlight(message, players);
    }

    private static void loadBundledPlayers() {
        try (InputStream stream = ChatNameHighlighter.class.getResourceAsStream(
            "/assets/hoplite_tweaks/highlighted-players.txt"
        )) {
            if (stream == null) {
                HopliteTweaks.LOGGER.warn("Bundled highlighted player list is missing");
                return;
            }
            List<PlayerHighlight> bundled = parsePlayers(
                new String(stream.readAllBytes(), StandardCharsets.UTF_8)
            );
            if (!bundled.isEmpty()) {
                players = bundled;
            }
        } catch (Exception exception) {
            HopliteTweaks.LOGGER.warn("Could not load the bundled highlighted player list");
        }
    }

    private static void loadCache() {
        try {
            Path path = cacheFile();
            if (Files.exists(path)) {
                List<PlayerHighlight> cached = parsePlayers(Files.readString(path));
                if (!cached.isEmpty()) {
                    players = cached;
                }
            }
        } catch (Exception exception) {
            HopliteTweaks.LOGGER.warn("Could not load the cached highlighted player list");
        }
    }

    private static void saveCache(String text) {
        try {
            Files.createDirectories(cacheDirectory());
            Files.writeString(cacheFile(), text, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            HopliteTweaks.LOGGER.warn("Could not cache the highlighted player list");
        }
    }

    private static Path cacheDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve("hoplite-tweaks");
    }

    private static Path cacheFile() {
        return cacheDirectory().resolve("highlighted-players.txt");
    }

    static List<PlayerHighlight> parsePlayers(String text) {
        List<PlayerHighlight> parsed = new ArrayList<>();
        for (String inputLine : text.split("\\R")) {
            String line = inputLine.replace("\uFEFF", "").trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\\s+");
            if (fields.length < 2
                || fields.length > 3
                || !VALID_USERNAME.matcher(fields[0]).matches()) {
                continue;
            }
            Integer color = parseHex(fields[1]);
            Boolean bold = fields.length == 2 ? Boolean.FALSE : parseWeight(fields[2]);
            if (color == null || bold == null) {
                continue;
            }
            String normalized = fields[0].toLowerCase(Locale.ROOT);
            parsed.removeIf(existing -> existing.name().equals(normalized));
            parsed.add(new PlayerHighlight(normalized, color, bold));
            if (parsed.size() >= MAX_PLAYERS) {
                break;
            }
        }
        parsed.sort(Comparator.comparingInt((PlayerHighlight entry) -> entry.name().length()).reversed());
        return List.copyOf(parsed);
    }

    private static Integer parseHex(String value) {
        String normalized = value.startsWith("#") ? value.substring(1) : value;
        if (!normalized.matches("[0-9a-fA-F]{6}")) {
            return null;
        }
        return Integer.parseInt(normalized, 16);
    }

    private static Boolean parseWeight(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "bold" -> Boolean.TRUE;
            case "normal", "plain" -> Boolean.FALSE;
            default -> null;
        };
    }

    static Component highlight(Component message, List<PlayerHighlight> highlights) {
        if (highlights.isEmpty()) {
            return message;
        }

        MutableComponent result = Component.empty();
        boolean changed = false;
        for (Component part : message.toFlatList()) {
            String text = part.getString();
            String lower = text.toLowerCase(Locale.ROOT);
            int cursor = 0;
            while (cursor < text.length()) {
                NameMatch match = findNextMatch(lower, cursor, highlights);
                if (match == null) {
                    result.append(Component.literal(text.substring(cursor)).setStyle(part.getStyle()));
                    break;
                }
                if (match.start() > cursor) {
                    result.append(Component.literal(text.substring(cursor, match.start()))
                        .setStyle(part.getStyle()));
                }
                Style highlightStyle = part.getStyle()
                    .withColor(TextColor.fromRgb(match.highlight().color()))
                    .withBold(match.highlight().bold());
                result.append(Component.literal(text.substring(match.start(), match.end()))
                    .setStyle(highlightStyle));
                cursor = match.end();
                changed = true;
            }
            if (text.isEmpty()) {
                result.append(part.copy());
            }
        }
        return changed ? result : message;
    }

    private static NameMatch findNextMatch(
        String text,
        int fromIndex,
        List<PlayerHighlight> highlights
    ) {
        NameMatch best = null;
        for (PlayerHighlight highlight : highlights) {
            String name = highlight.name();
            int start = text.indexOf(name, fromIndex);
            while (start >= 0) {
                int end = start + name.length();
                if ((start == 0 || !isUsernameCharacter(text.charAt(start - 1)))
                    && (end == text.length() || !isUsernameCharacter(text.charAt(end)))) {
                    if (best == null
                        || start < best.start()
                        || start == best.start() && end > best.end()) {
                        best = new NameMatch(start, end, highlight);
                    }
                    break;
                }
                start = text.indexOf(name, start + 1);
            }
        }
        return best;
    }

    private static boolean isUsernameCharacter(char character) {
        return character == '_' || character >= 'a' && character <= 'z'
            || character >= '0' && character <= '9';
    }

    record PlayerHighlight(String name, int color, boolean bold) {
    }

    private record NameMatch(int start, int end, PlayerHighlight highlight) {
    }
}
