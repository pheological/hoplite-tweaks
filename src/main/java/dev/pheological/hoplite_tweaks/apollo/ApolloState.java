package dev.pheological.hoplite_tweaks.apollo;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ApolloState {
    private static final Map<UUID, ApolloModels.Teammate> TEAMMATES = new ConcurrentHashMap<>();
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
        TEAMMATES.clear();
        for (ApolloModels.Teammate member : members) {
            TEAMMATES.put(member.playerId(), member);
        }
    }

    static void clearTeammates() {
        TEAMMATES.clear();
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
        TEAMMATES.clear();
        COOLDOWNS.clear();
    }
}
