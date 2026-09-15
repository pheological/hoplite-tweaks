package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloProtocol;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import dev.pheological.hoplite_tweaks.network.ApolloPayload;
import dev.pheological.hoplite_tweaks.network.ResetTeammatesPayload;
import dev.pheological.hoplite_tweaks.network.TeammatesPayload;
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
        ConfigKeybind.initialize();

        //? >=26 {
        /*PayloadTypeRegistry.clientboundPlay().register(ApolloPayload.TYPE, ApolloPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TeammatesPayload.TYPE, TeammatesPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ResetTeammatesPayload.TYPE, ResetTeammatesPayload.CODEC);
        *///?} else {
        PayloadTypeRegistry.playS2C().register(ApolloPayload.TYPE, ApolloPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TeammatesPayload.TYPE, TeammatesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ResetTeammatesPayload.TYPE, ResetTeammatesPayload.CODEC);
        //?}
        ClientPlayNetworking.registerGlobalReceiver(ApolloPayload.TYPE, (payload, context) -> {
            if (HopliteSession.isActive()) {
                ApolloProtocol.accept(payload.data());
            } else {
                ApolloState.clear();
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(TeammatesPayload.TYPE, (payload, context) -> {
            if (!HopliteSession.isActive()) return;
            HopliteTeammates.accept(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(ResetTeammatesPayload.TYPE,
            (payload, context) -> HopliteTeammates.reset());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ApolloState.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ApolloState.clear());

        HopliteHud.initialize();
        TeammateMarkerRenderer.initialize();
        SupplyBeams.initialize();
        KillCounter.initialize();
        HopliteAutomation.initialize();
        AutoApplySkins.initialize();
        AntiSlurFilter.initialize();
        NonRankDetector.initialize();
        MessageDelay.initialize();
        ChatNameHighlighter.initialize();
        LOGGER.info("Hoplite Tweaks initialized; features are gated to server addresses containing 'hoplite'");
    }
}
