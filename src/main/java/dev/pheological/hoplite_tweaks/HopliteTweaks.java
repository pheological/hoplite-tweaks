package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloProtocol;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import dev.pheological.hoplite_tweaks.network.ApolloPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HopliteTweaks {
    public static final String MOD_ID = "hoplite_tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private HopliteTweaks() {
    }

    public static void initializeClient() {
        HopliteTweaksConfig.load();

        //? >=26 {
        /*PayloadTypeRegistry.clientboundPlay().register(ApolloPayload.TYPE, ApolloPayload.CODEC);
        *///?} else {
        PayloadTypeRegistry.playS2C().register(ApolloPayload.TYPE, ApolloPayload.CODEC);
        //?}
        ClientPlayNetworking.registerGlobalReceiver(ApolloPayload.TYPE, (payload, context) -> {
            if (HopliteSession.isActive()) {
                ApolloProtocol.accept(payload.data());
            } else {
                ApolloState.clear();
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ApolloState.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ApolloState.clear());

        HopliteHud.initialize();
        TeammateMarkerRenderer.initialize();
        HopliteAutomation.initialize();
        HopliteNickDetector.initialize();
        LOGGER.info("Hoplite Tweaks initialized; features are gated to server addresses containing 'hoplite'");
    }
}
