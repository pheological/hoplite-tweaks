package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.network.TeammatesPayload;
import net.minecraft.world.phys.Vec3;

/** Applies Hoplite's authoritative teammate packets to the marker state. */
public final class HopliteTeammates {
    private HopliteTeammates() {}

    public static void accept(TeammatesPayload payload) {
        long now = System.currentTimeMillis();
        ApolloState.replaceTeammatesAuthoritative(payload.teammates().stream().map(member -> {
            Vec3 position = member.position();
            return new ApolloModels.Teammate(
                member.playerId(), position.x, position.y, position.z,
                member.dimension().identifier().toString(), member.displayName().getString(),
                member.markerColor(), now
            );
        }).toList());
    }

    public static void reset() {
        ApolloState.clearTeammates();
    }
}
