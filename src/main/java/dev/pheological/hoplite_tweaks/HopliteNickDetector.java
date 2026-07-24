package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Detects unregistered display names without attempting to identify the account behind them.
 */
public final class HopliteNickDetector {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final String PRIMARY_LOOKUP =
        "https://api.minecraftservices.com/minecraft/profile/lookup/name/";
    private static final String FALLBACK_LOOKUP =
        "https://api.mojang.com/users/profiles/minecraft/";
    private static final long JOIN_GRACE_PERIOD_MS = 3_000L;
    private static final long ROSTER_SCAN_INTERVAL_MS = 1_000L;
    private static final long MIN_REQUEST_INTERVAL_MS = 1_200L;
    private static final int PRIMARY_FAILURES_BEFORE_FALLBACK = 3;
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(4))
        .build();
    private static final Set<String> COMPLETED = new HashSet<>();
    private static final Set<String> PENDING = new HashSet<>();
    private static final PriorityQueue<LookupJob> LOOKUPS =
        new PriorityQueue<>(Comparator.comparingLong(LookupJob::readyAt));
    private static long joinedAt;
    private static long lastRosterScanAt;
    private static long nextRequestAt;
    private static boolean lookupInFlight;
    private static int scanGeneration;
    private static Object observedWorld;

    private HopliteNickDetector() {
    }

    public static void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetScan();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            joinedAt = 0;
            clearScanState();
            observedWorld = null;
        });
        ClientTickEvents.END_CLIENT_TICK.register(HopliteNickDetector::tick);
    }

    private static void tick(Minecraft client) {
        if (client.level != observedWorld) {
            observedWorld = client.level;
            resetScan();
        }
        long now = System.currentTimeMillis();
        if (joinedAt == 0
            || now - joinedAt < JOIN_GRACE_PERIOD_MS
            || !HopliteSession.isActive()
            || !HopliteTweaksConfig.get().enabled
            || !HopliteTweaksConfig.get().nickDetector
            || client.level == null
            || client.getConnection() == null) {
            return;
        }

        Objective sidebar = client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) {
            return;
        }
        String normalizedTitle = sidebar.getDisplayName().getString()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
        if (!normalizedTitle.contains("royale") || normalizedTitle.contains("hoplite royale")) {
            return;
        }

        if (now - lastRosterScanAt >= ROSTER_SCAN_INTERVAL_MS) {
            lastRosterScanAt = now;
            for (PlayerInfo player : client.getConnection().getOnlinePlayers()) {
                String name = player.getProfile().name();
                String key = name.toLowerCase(Locale.ROOT);
                if (USERNAME.matcher(name).matches()
                    && !COMPLETED.contains(key)
                    && PENDING.add(key)) {
                    LOOKUPS.add(new LookupJob(name, key, LookupProvider.PRIMARY, 0, now));
                }
            }
        }

        LookupJob next = LOOKUPS.peek();
        if (!lookupInFlight && next != null && next.readyAt() <= now && nextRequestAt <= now) {
            LOOKUPS.poll();
            lookupInFlight = true;
            nextRequestAt = now + MIN_REQUEST_INTERVAL_MS;
            checkName(client, next, scanGeneration);
        }
    }

    private static void checkName(Minecraft client, LookupJob job, int generation) {
        String endpoint = job.provider() == LookupProvider.PRIMARY ? PRIMARY_LOOKUP : FALLBACK_LOOKUP;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint + job.name()))
            .timeout(Duration.ofSeconds(6))
            .header("Accept", "application/json")
            .GET()
            .build();
        HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .whenComplete((response, error) -> client.execute(() -> {
                if (generation != scanGeneration) {
                    return;
                }
                lookupInFlight = false;
                if (error != null) {
                    retry(job, System.currentTimeMillis(), false);
                    return;
                }

                int status = response.statusCode();
                if (status == 200) {
                    complete(job);
                } else if (status == 404 || status == 204) {
                    complete(job);
                    showLikelyNick(job.name());
                } else {
                    long retryAt = System.currentTimeMillis();
                    if (status == 429) {
                        retryAt += retryAfterMillis(response);
                        nextRequestAt = Math.max(nextRequestAt, retryAt);
                    }
                    retry(job, retryAt, status == 429);
                }
            }));
    }

    private static void complete(LookupJob job) {
        PENDING.remove(job.key());
        COMPLETED.add(job.key());
    }

    private static void retry(LookupJob job, long now, boolean rateLimited) {
        int failures = job.failures() + 1;
        LookupProvider provider = job.provider();
        if (provider == LookupProvider.PRIMARY && failures >= PRIMARY_FAILURES_BEFORE_FALLBACK) {
            LOOKUPS.add(new LookupJob(
                job.name(), job.key(), LookupProvider.FALLBACK, 0,
                now + (rateLimited ? 30_000L : 1_000L)
            ));
            return;
        }
        long delay = rateLimited ? 30_000L : retryDelayMillis(failures);
        LOOKUPS.add(new LookupJob(job.name(), job.key(), provider, failures, now + delay));
    }

    private static long retryDelayMillis(int failures) {
        return Math.min(120_000L, 5_000L * (1L << Math.min(failures - 1, 5)));
    }

    private static long retryAfterMillis(HttpResponse<?> response) {
        String value = response.headers().firstValue("Retry-After").orElse("");
        try {
            return Math.max(30_000L, Long.parseLong(value) * 1_000L);
        } catch (NumberFormatException ignored) {
            try {
                long until = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant().toEpochMilli() - System.currentTimeMillis();
                return Math.max(30_000L, until);
            } catch (DateTimeParseException ignoredDate) {
                return 30_000L;
            }
        }
    }

    private static void showLikelyNick(String name) {
        if (!HopliteSession.isActive()) {
            return;
        }
        HopliteChat.send(
            Component.literal("[Hoplite Tweaks] ")
                .withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal(name).withStyle(ChatFormatting.RED))
                .append(Component.literal(" is likely nicked (display name is not registered).")
                    .withStyle(ChatFormatting.RED))
        );
    }

    private static void resetScan() {
        joinedAt = System.currentTimeMillis();
        clearScanState();
    }

    private static void clearScanState() {
        lastRosterScanAt = 0;
        nextRequestAt = 0;
        lookupInFlight = false;
        COMPLETED.clear();
        PENDING.clear();
        LOOKUPS.clear();
        scanGeneration++;
    }

    private enum LookupProvider {
        PRIMARY,
        FALLBACK
    }

    private record LookupJob(
        String name,
        String key,
        LookupProvider provider,
        int failures,
        long readyAt
    ) {
    }
}
