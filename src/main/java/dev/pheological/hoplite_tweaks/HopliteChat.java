package dev.pheological.hoplite_tweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class HopliteChat {
    private HopliteChat() {
    }

    static void send(Component message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        //? >=26.2 {
        /*client.gui.chatListener().handleSystemMessage(message, false);
        *///?} else if >=26 {
        /*client.gui.getChat().addClientSystemMessage(message);
        *///?} else {
        client.player.displayClientMessage(message, false);
        //?}
    }
}
