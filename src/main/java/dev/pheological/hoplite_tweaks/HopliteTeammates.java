package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.network.TeammatesPayload;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Applies Hoplite's authoritative teammate packets to the marker state. */
public final class HopliteTeammates {
    private HopliteTeammates() {}

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> acceptMessage(message.getString()));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, receivedAt) ->
            acceptMessage(message.getString()));
    }

    public static void accept(TeammatesPayload payload) {
        long now = System.currentTimeMillis();
        Minecraft client = Minecraft.getInstance();
        Set<UUID> onlinePlayers = client != null && client.getConnection() != null
            ? client.getConnection().getOnlinePlayers().stream()
                .map(info -> info.getProfile().id())
                .collect(Collectors.toSet())
            : Set.of();
        ApolloState.replaceTeammatesAuthoritative(payload.teammates().stream().map(member -> {
            if (client != null && client.getConnection() != null) {
                var info = client.getConnection().getPlayerInfo(member.playerId());
                if (info != null) {
                    ApolloState.observeProfile(member.playerId(), info.getProfile().name());
                }
            }
            Vec3 position = member.position();
            return new ApolloModels.Teammate(
                member.playerId(), position.x, position.y, position.z,
                member.dimension().identifier().toString(), member.displayName().getString(),
                member.markerColor(), now
            );
        }).toList(), onlinePlayers);
    }

    public static void reset() {
        ApolloState.clearTeammates();
    }

    static void acceptMessage(String message) {
        if (HopliteSession.isActive()) {
            ApolloState.expireCorpse(message);
        }
    }
}
