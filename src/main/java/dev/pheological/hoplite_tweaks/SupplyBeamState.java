package dev.pheological.hoplite_tweaks;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Session-only tracking, independent of Minecraft so lifecycle rules can be tested. */
final class SupplyBeamState {
    static final double UNLOADED_GROUND_Y = 64.0D;
    static final long LIFETIME_MS = 5 * 60 * 1000L;
    private static final Pattern COORDINATE = Pattern.compile(
        "(?i)(?<![a-z0-9_])([xz])\\s*[:=]\\s*([+-]?\\d+)"
    );
    private final Map<Location, Drop> drops = new LinkedHashMap<>();
    // Keep recently cleared locations too, so duplicate delivery cannot resurrect a visited drop.
    private final Map<Location, Long> seen = new LinkedHashMap<>();
    private Object world;

    record Location(int x, int z) { }
    record Drop(Location position, double groundY, long announcedAt) { }

    static Location parse(String message) {
        if (message == null) return null;
        String plain = message.replaceAll("(?i)§[0-9a-fk-orx]", "").replaceAll("\\s+", " ").trim();
        if (!plain.toLowerCase(Locale.ROOT).contains("supply drop")) return null;

        Integer x = null;
        Integer z = null;
        try {
            Matcher match = COORDINATE.matcher(plain);
            while (match.find()) {
                int value = Integer.parseInt(match.group(2));
                if (match.group(1).equalsIgnoreCase("x")) {
                    if (x != null && x != value) return null;
                    x = value;
                } else {
                    if (z != null && z != value) return null;
                    z = value;
                }
            }
            if (x == null || z == null) return null;
            if (Math.abs((long) x) > 30_000_000 || Math.abs((long) z) > 30_000_000) return null;
            return new Location(x, z);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    void enterWorld(Object currentWorld) {
        if (world != currentWorld) {
            clear();
            seen.clear();
            world = currentWorld;
        }
    }

    void announce(Location location, long now) {
        if (location == null || world == null) return;
        Long previous = seen.get(location);
        if (previous != null && now - previous < LIFETIME_MS) return;
        seen.put(location, now);
        drops.put(location, new Drop(location, UNLOADED_GROUND_Y, now));
    }

    void tick(long now, double playerX, double playerZ, int radius) {
        seen.entrySet().removeIf(entry -> now - entry.getValue() >= LIFETIME_MS);
        drops.values().removeIf(drop -> now - drop.announcedAt >= LIFETIME_MS
            || (radius > 0 && Math.hypot(drop.position.x - playerX, drop.position.z - playerZ) <= radius));
    }

    void groundHeight(Location location, Integer loadedSurfaceY) {
        // An unloaded chunk provides no new information. Retain the last observed surface
        // instead of dropping back to the initial Y=64 estimate at the render-distance boundary.
        if (loadedSurfaceY == null) return;
        drops.computeIfPresent(location, (key, drop) -> new Drop(key, loadedSurfaceY, drop.announcedAt));
    }

    boolean isWorld(Object currentWorld) { return world == currentWorld; }

    List<Drop> drops() { return List.copyOf(drops.values()); }
    void clear() { drops.clear(); }
    void reset() {
        clear();
        seen.clear();
        world = null;
    }

    /** Uniform projection keeps every point on the same camera ray and inside the far plane. */
    static double projectionScale(double x, double y, double z, double height, double width, double limit) {
        double farthest = Math.max(Math.sqrt(x * x + y * y + z * z),
            Math.sqrt(x * x + (y + height) * (y + height) + z * z)) + width;
        return farthest > limit ? limit / farthest : 1.0D;
    }
}
