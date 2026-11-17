package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloProtocol;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.apollo.ApolloCompatibility;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import dev.pheological.hoplite_tweaks.network.ApolloPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
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
        SlotLock.initialize();
        TeamTrackerKeybind.initialize();
        QuakeProKeybind.initialize();
        QuickcraftKeybind.initialize();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> initializeApollo());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ApolloState.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ApolloState.clear());

        HopliteHud.initialize();
        HopliteTeammates.initialize();
        TeammateMarkerRenderer.initialize();
        SupplyBeams.initialize();
        KillCounter.initialize();
        AutoDamageSummary.initialize();
        HopliteAutomation.initialize();
        AutoApplySkins.initialize();
        AntiSlurFilter.initialize();
        NonRankDetector.initialize();
        MessageDelay.initialize();
        ChatNameHighlighter.initialize();
        LOGGER.info("Hoplite Tweaks initialized; features are gated to server addresses containing 'hoplite'");
    }

    private static void initializeApollo() {
        // Preserve an existing owner's codec and payload class, as well as Lunar's native handling.
        //? >=26 {
        /*var apolloRegistry = PayloadTypeRegistry.clientboundPlay();
        *///?} else {
        var apolloRegistry = PayloadTypeRegistry.playS2C();
        //?}
        boolean registerApollo = ApolloCompatibility.registerChannel(
            HopliteTweaks.class.getClassLoader(), apolloRegistry, () ->
                ClientPlayNetworking.registerGlobalReceiver(ApolloPayload.TYPE, (payload, context) -> {
                    if (HopliteSession.isActive()) {
                        ApolloProtocol.accept(payload.data());
                    } else {
                        ApolloState.clear();
                    }
                }));
        if (!registerApollo) {
            LOGGER.info("Existing Apollo handler detected; observing team and cooldown messages without taking ownership of lunar:apollo");
        }
    }

}
