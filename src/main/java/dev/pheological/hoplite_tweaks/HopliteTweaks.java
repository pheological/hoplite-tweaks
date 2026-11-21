package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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
}
