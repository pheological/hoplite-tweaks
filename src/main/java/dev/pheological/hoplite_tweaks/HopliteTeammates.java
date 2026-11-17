package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import java.util.Collection;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Applies Apollo team snapshots and observes Hoplite corpse notifications. */
public final class HopliteTeammates {
    private HopliteTeammates() {}

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> acceptMessage(message.getString()));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, receivedAt) ->
            acceptMessage(message.getString()));
    }

    public static void accept(Collection<ApolloModels.Teammate> members) {
        Minecraft client = Minecraft.getInstance();
        Set<UUID> onlinePlayers = client != null && client.getConnection() != null
            ? client.getConnection().getOnlinePlayers().stream()
                .map(info -> info.getProfile().id()).collect(Collectors.toSet()) : Set.of();
        if (client != null && client.getConnection() != null) {
            client.getConnection().getOnlinePlayers().forEach(info ->
                ApolloState.observeProfile(info.getProfile().id(), info.getProfile().name()));
        }
        ApolloState.replaceTeammatesAuthoritative(members, onlinePlayers);
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
