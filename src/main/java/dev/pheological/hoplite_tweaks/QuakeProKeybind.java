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

public final class QuakeProKeybind {
    private static KeyMapping toggleKey;
    private static final QuakeProState STATE = new QuakeProState();
    private static QuakeProState.Settings saving;

    private QuakeProKeybind() {
    }

    static void initialize() {
        //? >=26 {
        /*toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?} else {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?}
            //? >=26.3 {
            /*"key.hoplite_tweaks.toggle_quake_pro", InputConstants.Type.KEYBOARD,
            *///?} else {
            "key.hoplite_tweaks.toggle_quake_pro", InputConstants.Type.KEYSYM,
            //?}
            InputConstants.UNKNOWN.getValue(),
            HopliteKeybindings.CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(QuakeProKeybind::tick);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT
            .register((handler, client) -> restore(client.options));
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING
            .register(client -> restore(client.options));
    }

    private static void tick(Minecraft client) {
        boolean available = HopliteTweaksConfig.get().enabled && HopliteSession.isActive()
            && client.player != null;
        if (!available) restore(client.options);
        while (toggleKey.consumeClick()) {
            //? >=26.2 {
            /*boolean screenOpen = client.gui.screen() != null;
            *///?} else {
            boolean screenOpen = client.screen != null;
            //?}
            if (available && !screenOpen) {
                int maximum = ((net.minecraft.client.OptionInstance.IntRange)
                    client.options.fov().values()).maxInclusive();
                apply(client.options, STATE.toggle(read(client.options), maximum,
                    HopliteTweaksConfig.get().autoViewBobbing));
            }
        }
    }

    private static QuakeProState.Settings read(net.minecraft.client.Options options) {
        return new QuakeProState.Settings(options.fov().get(), options.bobView().get());
    }

    private static void apply(net.minecraft.client.Options options, QuakeProState.Settings settings) {
        options.fov().set(settings.fov());
        options.bobView().set(settings.bobbing());
    }

    private static void restore(net.minecraft.client.Options options) {
        if (STATE.active()) apply(options, STATE.restore(read(options)));
    }

    /** Temporarily expose original preferences to vanilla's options writer. */
    public static void beforeOptionsSave(net.minecraft.client.Options options) {
        if (STATE.active() && saving == null) {
            saving = read(options);
            apply(options, STATE.preferences(saving));
        }
    }

    public static void afterOptionsSave(net.minecraft.client.Options options) {
        if (saving != null) {
            apply(options, saving);
            saving = null;
        }
    }
}
