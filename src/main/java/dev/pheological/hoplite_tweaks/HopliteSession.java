package dev.pheological.hoplite_tweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Locale;

/**
 * The single safety gate for every gameplay feature in this mod.
 */
public final class HopliteSession {
    private HopliteSession() {
    }

    public static boolean isActive() {
        Minecraft client = Minecraft.getInstance();
        ServerData server = client.getCurrentServer();
        return server != null
            && server.ip != null
            && server.ip.toLowerCase(Locale.ROOT).contains("hoplite");
    }
}
