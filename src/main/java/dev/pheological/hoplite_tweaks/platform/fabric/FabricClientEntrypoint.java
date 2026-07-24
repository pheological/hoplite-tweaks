package dev.pheological.hoplite_tweaks.platform.fabric;

import dev.pheological.hoplite_tweaks.HopliteTweaks;
import net.fabricmc.api.ClientModInitializer;

public final class FabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HopliteTweaks.initializeClient();
    }
}
