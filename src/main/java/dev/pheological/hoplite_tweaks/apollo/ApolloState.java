package dev.pheological.hoplite_tweaks.apollo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.minecraft.world.phys.Vec3;

public final class ApolloState {
    private static final Map<UUID, ApolloModels.Teammate> TEAMMATES = new ConcurrentHashMap<>();
    private static final Map<LastKnownKey, ApolloModels.LastKnownTeammate> LAST_KNOWN =
        new ConcurrentHashMap<>();
    private static final Map<UUID, ApolloModels.DeathLocation> DEATH_LOCATIONS =
        new ConcurrentHashMap<>();
    private static final Map<UUID, ApolloModels.Teammate> PENDING_DISCONNECTS =
        new ConcurrentHashMap<>();
    private static final Map<UUID, String> PROFILE_NAMES = new ConcurrentHashMap<>();
    private static final Set<UUID> EXPIRED_CORPSES = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, TeammateMotion> MOTION = new ConcurrentHashMap<>();
    private static final Map<String, ApolloModels.Cooldown> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Pattern FORMATTING = Pattern.compile("(?i)§[0-9a-fk-orx]");
    private static final Pattern PROFILE_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private ApolloState() {
    }

    public static Collection<ApolloModels.Teammate> teammates() {
        return TEAMMATES.values();
    }

    public static boolean isTeammate(UUID playerId) {
        return TEAMMATES.containsKey(playerId);
    }

    public static Collection<ApolloModels.LastKnownTeammate> lastKnownTeammates() {
        return LAST_KNOWN.values();
    }

    public static Collection<ApolloModels.DeathLocation> deathLocations() {
        return DEATH_LOCATIONS.values();
    }

    public static void observeProfile(UUID playerId, String profileName) {
        if (playerId != null && profileName != null && PROFILE_NAME.matcher(profileName).matches()) {
            PROFILE_NAMES.put(playerId, profileName);
        }
    }

    static void replaceTeammates(Collection<ApolloModels.Teammate> members) {
        replaceTeammates(members, Set.of());
    }

    static void replaceTeammates(Collection<ApolloModels.Teammate> members, Set<UUID> onlinePlayers) {
        long now = System.nanoTime() / 1_000_000L;
        Map<UUID, ApolloModels.Teammate> previous = new HashMap<>(TEAMMATES);
        TEAMMATES.clear();
        for (ApolloModels.Teammate member : members) {
            ApolloModels.Teammate prior = previous.remove(member.playerId());
            // A dimension change is not a disconnect, so do not leave a stale marker.
            PENDING_DISCONNECTS.remove(member.playerId());
            LAST_KNOWN.keySet().removeIf(key -> key.playerId().equals(member.playerId()));
            MOTION.compute(member.playerId(), (id, motion) -> new TeammateMotion(member, motion, now));
            TEAMMATES.put(member.playerId(), member);
        }
        previous.values().forEach(teammate -> {
            if (onlinePlayers.contains(teammate.playerId())) {
                PENDING_DISCONNECTS.put(teammate.playerId(), teammate);
            } else {
                archive(teammate);
            }
        });
        PENDING_DISCONNECTS.entrySet().removeIf(entry -> {
            if (onlinePlayers.contains(entry.getKey())) {
                return false;
            }
            archive(entry.getValue());
            return true;
        });
        MOTION.keySet().retainAll(TEAMMATES.keySet());
    }

    /** Applies an authoritative Hoplite snapshot while smoothing its sparse remote positions. */
    public static void replaceTeammatesAuthoritative(Collection<ApolloModels.Teammate> members) {
        replaceTeammates(members, Set.of());
    }

    public static void replaceTeammatesAuthoritative(
        Collection<ApolloModels.Teammate> members,
        Set<UUID> onlinePlayers
    ) {
        replaceTeammates(members, onlinePlayers == null ? Set.of() : onlinePlayers);
    }

    public static Vec3 teammatePosition(ApolloModels.Teammate teammate) {
        TeammateMotion motion = MOTION.get(teammate.playerId());
        return motion == null ? new Vec3(teammate.x(), teammate.y(), teammate.z())
            : motion.position(System.nanoTime() / 1_000_000L);
    }

    public static boolean expireCorpse(String message) {
        String normalized = normalize(message);
        if (!normalized.contains("corpse has died")) {
            return false;
        }
        var expired = PROFILE_NAMES.entrySet().stream()
            .filter(entry -> normalized.contains(
                entry.getValue().toLowerCase(java.util.Locale.ROOT) + " s corpse has died"))
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
        if (expired.isEmpty()) {
            return false;
        }
        EXPIRED_CORPSES.addAll(expired);
        LAST_KNOWN.keySet().removeIf(key -> expired.contains(key.playerId()));
        return true;
    }

    public static boolean markDeath(UUID playerId, long diedAt) {
        if (playerId == null) {
            return false;
        }
        ApolloModels.Teammate sample = TEAMMATES.get(playerId);
        if (sample == null) {
            sample = PENDING_DISCONNECTS.get(playerId);
        }
        if (sample == null) {
            sample = LAST_KNOWN.entrySet().stream()
                .filter(entry -> entry.getKey().playerId().equals(playerId))
                .map(entry -> entry.getValue().teammate())
                .max(java.util.Comparator.comparingLong(ApolloModels.Teammate::updatedAt))
                .orElse(null);
        }
        if (sample == null) {
            return false;
        }
        String profileName = PROFILE_NAMES.get(playerId);
        if (profileName == null && PROFILE_NAME.matcher(sample.displayName()).matches()) {
            profileName = sample.displayName();
        }
        DEATH_LOCATIONS.put(playerId, new ApolloModels.DeathLocation(sample, profileName, diedAt));
        PENDING_DISCONNECTS.remove(playerId);
        LAST_KNOWN.keySet().removeIf(key -> key.playerId().equals(playerId));
        EXPIRED_CORPSES.add(playerId);
        return true;
    }

    public static void clearTeammates() {
        TEAMMATES.clear();
        LAST_KNOWN.clear();
        DEATH_LOCATIONS.clear();
        PENDING_DISCONNECTS.clear();
        PROFILE_NAMES.clear();
        EXPIRED_CORPSES.clear();
        MOTION.clear();
    }

    public static Collection<ApolloModels.Cooldown> cooldowns() {
        COOLDOWNS.values().removeIf(cooldown -> cooldown.remainingMillis(System.currentTimeMillis()) == 0);
        return COOLDOWNS.values();
    }

    static void putCooldown(ApolloModels.Cooldown cooldown) {
        COOLDOWNS.put(cooldown.name(), cooldown);
    }

    static void removeCooldown(String name) {
        COOLDOWNS.remove(name);
    }

    public static void clearCooldowns() {
        COOLDOWNS.clear();
    }

    public static void clear() {
        clearTeammates();
        COOLDOWNS.clear();
    }

    private static void archive(ApolloModels.Teammate teammate) {
        if (EXPIRED_CORPSES.contains(teammate.playerId())) {
            return;
        }
        String profileName = PROFILE_NAMES.get(teammate.playerId());
        if (profileName == null && PROFILE_NAME.matcher(teammate.displayName()).matches()) {
            profileName = teammate.displayName();
        }
        LAST_KNOWN.put(
            new LastKnownKey(teammate.playerId(), teammate.world()),
            new ApolloModels.LastKnownTeammate(teammate, profileName)
        );
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return FORMATTING.matcher(text).replaceAll("")
            .replaceAll("[^A-Za-z0-9_]+", " ")
            .trim()
            .toLowerCase(java.util.Locale.ROOT);
    }

    private record LastKnownKey(UUID playerId, String world) {
    }
}
