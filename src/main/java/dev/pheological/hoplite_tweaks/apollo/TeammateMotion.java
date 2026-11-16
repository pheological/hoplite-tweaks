package dev.pheological.hoplite_tweaks.apollo;

import net.minecraft.world.phys.Vec3;

/** Smooths sparse remote-player snapshots over the interval observed between packets. */
final class TeammateMotion {
    private final ApolloModels.Teammate sample;
    private final long receivedAt;
    private final Vec3 start;
    private final Vec3 target;
    private final long transitionMillis;

    TeammateMotion(ApolloModels.Teammate sample, TeammateMotion previous, long now) {
        this.sample = sample;
        receivedAt = now;
        target = new Vec3(sample.x(), sample.y(), sample.z());
        long interval = previous == null ? 0 : now - previous.receivedAt;
        Vec3 movement = previous == null ? Vec3.ZERO : target.subtract(previous.target);
        // Do not animate world changes, teleports, or implausibly stale samples.
        boolean continuous = previous != null && sample.world().equals(previous.sample.world())
            && interval >= 50 && interval <= 10_000 && movement.length() <= 64
            && movement.length() / interval <= 0.02;
        start = continuous ? previous.position(now) : target;
        transitionMillis = continuous ? interval : 0;
    }

    Vec3 position(long now) {
        if (transitionMillis == 0) {
            return target;
        }
        double progress = Math.clamp((double) Math.max(0, now - receivedAt) / transitionMillis, 0.0, 1.0);
        return start.lerp(target, progress);
    }
}
