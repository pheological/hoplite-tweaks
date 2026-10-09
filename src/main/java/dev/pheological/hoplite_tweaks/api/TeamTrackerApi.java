package dev.pheological.hoplite_tweaks.api;

import dev.pheological.hoplite_tweaks.HopliteSession;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.client.Minecraft;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Optional client integration for compass HUDs. Call on the Minecraft client thread. */
public final class TeamTrackerApi {
    private TeamTrackerApi() {}

    /**
     * Immutable live teammate snapshot; empty outside Hoplite or when the tracker is disabled.
     * UUIDs can be resolved through the consumer's own player-info/skin renderer.
     * No Minecraft classes occur in the public signature, allowing optional reflective access.
     */
    public static List<Teammate> teammates() {
        Minecraft client = Minecraft.getInstance();
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (!HopliteSession.isActive() || !config.enabled || !config.teammateMarkers
            || client.player == null || client.level == null) {
            return List.of();
        }
        String dimension = client.level.dimension().identifier().toString();
        UUID self = client.player.getUUID();
        float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        var origin = client.player.getPosition(partialTick);
        var dead = ApolloState.deathLocations().stream()
            .map(entry -> entry.teammate().playerId()).collect(java.util.stream.Collectors.toSet());
        return ApolloState.teammates().stream()
            .filter(member -> !member.playerId().equals(self) && !dead.contains(member.playerId()))
            .filter(member -> sameDimension(member.world(), dimension))
            .map(member -> {
                var loaded = client.level.players().stream()
                    .filter(player -> player.getUUID().equals(member.playerId()))
                    .findFirst().orElse(null);
                if (loaded != null && (!loaded.isAlive() || loaded.getHealth() <= 0)) return null;
                var position = loaded == null ? ApolloState.teammatePosition(member)
                    : loaded.getPosition(partialTick);
                return new Teammate(member.playerId(), member.displayName(), member.color(),
                    position.x, position.y, position.z,
                    bearing(position.x - origin.x, position.z - origin.z),
                    position.distanceTo(origin));
            })
            .filter(java.util.Objects::nonNull)
            .sorted(Comparator.comparing(Teammate::playerId))
            .toList();
    }

    static boolean sameDimension(String world, String dimension) {
        return world.isBlank() || world.equals(dimension)
            || (!world.contains(":") && dimension.equals("minecraft:" + world));
    }

    static double bearing(double dx, double dz) {
        return (Math.toDegrees(Math.atan2(dx, -dz)) + 360.0D) % 360.0D;
    }

    /** Bearing uses compass degrees: north 0, east 90, south 180, west 270. Distance is in blocks. */
    public record Teammate(UUID playerId, String displayName, int color,
                           double x, double y, double z, double bearing, double distance) {}
}
