package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? >=26 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class SupplyBeams {
    static final SupplyBeamState STATE = new SupplyBeamState();
    private static KeyMapping toggleKey;

    private SupplyBeams() { }

    public static void initialize() {
        //? >=26 {
        /*toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?} else {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?}
            "key.hoplite_tweaks.toggle_supply_beams", InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "main"))));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> STATE.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> STATE.reset());
        // Server system chat only; player chat must never create a waypoint.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            Minecraft client = Minecraft.getInstance();
            if (!overlay && HopliteSession.isActive() && client.player != null && client.level != null) {
                syncWorld(client);
                STATE.announce(SupplyBeamState.parse(message.getString()), now());
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(SupplyBeams::tick);
        SupplyBeamRenderer.initialize();
    }

    private static long now() { return System.nanoTime() / 1_000_000L; }

    private static void syncWorld(Minecraft client) {
        STATE.enterWorld(client.level);
    }

    private static void tick(Minecraft client) {
        while (toggleKey.consumeClick()) {
            if (client.level != null && client.player != null && HopliteSession.isActive()) {
                HopliteTweaksConfig config = HopliteTweaksConfig.get();
                config.supplyCrateBeams = !config.supplyCrateBeams;
                HopliteTweaksConfig.save();
                HopliteChat.send(net.minecraft.network.chat.Component.literal(
                    "Supply crate beams: " + (config.supplyCrateBeams ? "ON" : "OFF")));
            }
        }
        if (client.level == null || client.player == null) {
            STATE.enterWorld(null);
            return;
        }
        syncWorld(client);
        STATE.tick(now(), client.player.getX(), client.player.getZ(),
            HopliteTweaksConfig.get().supplyBeamArrivalRadius);
        for (var drop : STATE.drops()) {
            int x = drop.position().x();
            int z = drop.position().z();
            // ClientLevel.hasChunk() always returns true, even outside the client cache.
            // required=false returns null rather than the empty-chunk placeholder whose
            // heightmap would incorrectly put the beam at the world's minimum Y (-64).
            var chunk = client.level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
            STATE.groundHeight(drop.position(), chunk == null ? null
                : chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15) + 1);
        }
    }

    public static void clearTrackedDrops() { STATE.clear(); }

}
