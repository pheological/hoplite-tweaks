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
import net.minecraft.network.chat.ClickEvent;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class QuickcraftKeybind {
    private static KeyMapping quickcraftKey;
    private static String latestCommand = "";

    private QuickcraftKeybind() {
    }

    static void initialize() {
        //? >=26 {
        /*quickcraftKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?} else {
        quickcraftKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?}
            //? >=26.3 {
            /*"key.hoplite_tweaks.quickcraft_most_recent", InputConstants.Type.KEYBOARD,
            *///?} else {
            "key.hoplite_tweaks.quickcraft_most_recent", InputConstants.Type.KEYSYM,
            //?}
            InputConstants.UNKNOWN.getValue(),
            HopliteKeybindings.CATEGORY));
        //? <26.3 {
        ClientReceiveMessageEvents.GAME.register(QuickcraftKeybind::onGameMessage);
        //?}
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, receivedAt) ->
            remember(message));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> latestCommand = "");
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> latestCommand = "");
        ClientTickEvents.END_CLIENT_TICK.register(QuickcraftKeybind::tick);
    }

    public static void onGameMessage(Component message, boolean overlay) {
        if (!overlay) {
            remember(message);
        }
    }

    private static void remember(Component message) {
        if (HopliteTweaksConfig.get().enabled && HopliteSession.isActive()) {
            String command = commandFromPrompt(message);
            if (!command.isBlank()) {
                latestCommand = command;
            }
        }
    }

    static String commandFromPrompt(Component message) {
        if (message == null || !message.getString().toLowerCase(java.util.Locale.ROOT)
            .contains("you have all the items to craft")) {
            return "";
        }
        return message.toFlatList().stream()
            .map(part -> part.getStyle().getClickEvent())
            .filter(ClickEvent.RunCommand.class::isInstance)
            .map(ClickEvent.RunCommand.class::cast)
            .map(ClickEvent.RunCommand::command)
            .filter(command -> !command.isBlank())
            .findFirst()
            .orElse("");
    }

    private static void tick(Minecraft client) {
        while (quickcraftKey.consumeClick()) {
            if (HopliteTweaksConfig.get().enabled && HopliteSession.isActive()
                && client.getConnection() != null && !latestCommand.isBlank()) {
                client.getConnection().sendCommand(
                    latestCommand.startsWith("/") ? latestCommand.substring(1) : latestCommand
                );
            }
        }
    }
}
