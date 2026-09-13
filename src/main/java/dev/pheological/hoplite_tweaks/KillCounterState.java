package dev.pheological.hoplite_tweaks;

import java.util.*;
import java.util.regex.Pattern;

/** Session-only match state; deliberately independent of Minecraft and display settings. */
final class KillCounterState {
    private static final Pattern FORMATTING = Pattern.compile("(?i)§[0-9a-fk-orx]");
    private final Map<String, Set<UUID>> profiles = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<String, Long> recent = new HashMap<>();
    private Object world;
    private long missingSince = -1;
    private boolean inGame;
    private boolean trackingKills = true;

    static String clean(String text) {
        if (text == null) return "";
        return FORMATTING.matcher(text).replaceAll("")
            .replaceAll("[^A-Za-z0-9_]+", " ").trim().toLowerCase(Locale.ROOT);
    }

    void observe(String name, UUID id) {
        if (name != null && id != null && name.matches("[A-Za-z0-9_]{1,16}")) {
            profiles.computeIfAbsent(name.toLowerCase(Locale.ROOT), key -> new HashSet<>()).add(id);
        }
    }

    UUID resolve(String word) {
        Set<UUID> matches = profiles.get(word.toLowerCase(Locale.ROOT));
        return matches != null && matches.size() == 1 ? matches.iterator().next() : null;
    }

    record Kill(UUID victim, UUID attacker) {}

    Kill parse(String text) {
        String[] words = clean(text).split(" ");
        int was = -1, by = -1;
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("was")) {
                if (was != -1) return null;
                was = i;
            }
            if (words[i].equals("by")) {
                if (by != -1) return null;
                by = i;
            }
        }
        if (was < 1 || by <= was + 1 || by >= words.length - 1) return null;
        Kill pair = pair(words[0], words[words.length - 1]);
        return pair != null ? pair : pair(words[was - 1], words[by + 1]);
    }

    private Kill pair(String victim, String attacker) {
        UUID v = resolve(victim), a = resolve(attacker);
        return v == null || a == null || v.equals(a) ? null : new Kill(v, a);
    }

    boolean accept(String text, long now) {
        if (!trackingKills()) return false;
        Kill kill = parse(text);
        if (kill == null) return false;
        recent.entrySet().removeIf(entry -> now - entry.getValue() >= 2_000);
        String normalized = clean(text);
        if (recent.containsKey(normalized)) return false;
        recent.put(normalized, now);
        kills.merge(kill.attacker(), 1, Integer::sum);
        return true;
    }

    /** Null sidebar means the new world's sidebar has not arrived yet. */
    void update(Object nextWorld, boolean hoplite, List<String> sidebar, long now,
        boolean afterMiningPhase) {
        if (!hoplite || nextWorld == null) {
            clear();
            world = nextWorld;
            return;
        }
        if (world != nextWorld) {
            world = nextWorld;
            inGame = false;
            missingSince = now;
        }
        if (sidebar == null) {
            inGame = false;
            if (missingSince < 0) missingSince = now;
            if (now - missingSince >= 5_000) resetMatch();
            return;
        }
        missingSince = -1;
        inGame = sidebar.stream().anyMatch(line -> clean(line).contains("game stats") || clean(line).contains("your stats"));
        trackingKills = !afterMiningPhase || sidebar.stream()
            .noneMatch(line -> clean(line).contains("mining phase in"));
        if (!inGame) resetMatch();
    }

    UUID playerInRow(String text) {
        UUID found = null;
        int matches = 0;
        for (String word : clean(text).split(" ")) {
            if (!profiles.containsKey(word)) continue;
            UUID id = resolve(word);
            if (id == null || ++matches > 1) return null;
            found = id;
        }
        return found;
    }

    boolean inGame() { return inGame; }
    boolean trackingKills() { return inGame && trackingKills; }
    int count(UUID id) { return kills.getOrDefault(id, 0); }
    void resetCounts() { kills.clear(); recent.clear(); }
    private void resetMatch() { resetCounts(); profiles.clear(); }
    void clear() { resetMatch(); world = null; inGame = false; trackingKills = true; missingSince = -1; }
}
