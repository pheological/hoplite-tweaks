package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? >=26 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

final class ConfigKeybind {
    private static KeyMapping openConfigKey;

    private ConfigKeybind() {
    }

    static void initialize() {
        //? >=26 {
        /*openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?} else {
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?}
            "key.hoplite_tweaks.open_config", InputConstants.Type.KEYSYM,
            InputConstants.KEY_P,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(ConfigKeybind::tick);
    }

    private static void tick(Minecraft client) {
        while (openConfigKey.consumeClick()) {
            //? >=26.2 {
            /*if (client.gui.screen() == null) {
            *///?} else {
            if (client.screen == null) {
            //?}
                //? >=26.2 {
                /*client.gui.setScreen(HopliteTweaksConfigScreen.create(null));
                *///?} else {
                client.setScreen(HopliteTweaksConfigScreen.create(null));
                //?}
            }
        }
    }
}
