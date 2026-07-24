package dev.pheological.hoplite_tweaks.apollo;

import java.util.UUID;

public final class ApolloModels {
    private ApolloModels() {
    }

    public record Teammate(
        UUID playerId,
        double x,
        double y,
        double z,
        String world,
        String displayName,
        int color,
        long updatedAt
    ) {
    }

    public record Cooldown(String name, String itemId, long startedAt, long durationMillis) {
        public float progress(long now) {
            if (durationMillis <= 0) {
                return 1.0F;
            }
            return Math.clamp((float) (now - startedAt) / durationMillis, 0.0F, 1.0F);
        }

        public long remainingMillis(long now) {
            return Math.max(0, startedAt + durationMillis - now);
        }
    }
}
