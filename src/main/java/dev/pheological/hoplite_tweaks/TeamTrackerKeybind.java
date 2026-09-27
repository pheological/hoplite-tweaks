package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? >=26 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class TeamTrackerKeybind {
    private static KeyMapping toggleKey;

    private TeamTrackerKeybind() {
    }

    static void initialize() {
        //? >=26 {
        /*toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?} else {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?}
            //? >=26.3 {
            /*"key.hoplite_tweaks.toggle_team_tracker", InputConstants.Type.KEYBOARD,
            *///?} else {
            "key.hoplite_tweaks.toggle_team_tracker", InputConstants.Type.KEYSYM,
            //?}
            InputConstants.UNKNOWN.getValue(),
            HopliteKeybindings.CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(TeamTrackerKeybind::tick);
    }

    private static void tick(Minecraft client) {
        while (toggleKey.consumeClick()) {
            HopliteTweaksConfig config = HopliteTweaksConfig.get();
            config.teammateMarkers = !config.teammateMarkers;
            HopliteTweaksConfig.save();
            HopliteChat.send(Component.literal(
                "Team tracker: " + (config.teammateMarkers ? "ON" : "OFF")));
        }
    }
}
