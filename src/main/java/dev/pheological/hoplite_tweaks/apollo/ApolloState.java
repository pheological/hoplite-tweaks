package dev.pheological.hoplite_tweaks.apollo;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.phys.Vec3;

public final class ApolloState {
    private static final Map<UUID, ApolloModels.Teammate> TEAMMATES = new ConcurrentHashMap<>();
    private static final Map<UUID, TeammateMotion> MOTION = new ConcurrentHashMap<>();
    private static final Map<String, ApolloModels.Cooldown> COOLDOWNS = new ConcurrentHashMap<>();

    private ApolloState() {
    }

    public static Collection<ApolloModels.Teammate> teammates() {
        return TEAMMATES.values();
    }

    public static boolean isTeammate(UUID playerId) {
        return TEAMMATES.containsKey(playerId);
    }

    static void replaceTeammates(Collection<ApolloModels.Teammate> members) {
        long now = System.nanoTime() / 1_000_000L;
        TEAMMATES.clear();
        for (ApolloModels.Teammate member : members) {
            MOTION.compute(member.playerId(), (id, previous) -> new TeammateMotion(member, previous, now));
            TEAMMATES.put(member.playerId(), member);
        }
        MOTION.keySet().retainAll(TEAMMATES.keySet());
    }

    /** Applies an authoritative Hoplite snapshot while smoothing its sparse remote positions. */
    public static void replaceTeammatesAuthoritative(Collection<ApolloModels.Teammate> members) {
        replaceTeammates(members);
    }

    public static Vec3 teammatePosition(ApolloModels.Teammate teammate) {
        TeammateMotion motion = MOTION.get(teammate.playerId());
        return motion == null ? new Vec3(teammate.x(), teammate.y(), teammate.z())
            : motion.position(System.nanoTime() / 1_000_000L);
    }

    public static void clearTeammates() {
        TEAMMATES.clear();
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
}
