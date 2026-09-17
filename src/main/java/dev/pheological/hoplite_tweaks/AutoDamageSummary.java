package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.regex.Pattern;

/** Requests Hoplite's public damage summary once when the second round begins. */
public final class AutoDamageSummary {
    private static final Pattern ROUND_ONE = Pattern.compile("\\bround\\s+1\\b");
    private static final Pattern ROUND_TWO = Pattern.compile("\\bround\\s+2\\b");
    private static boolean sentForRoundTwo;

    private AutoDamageSummary() {
    }

    public static void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    public static void onTitle(Component title) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (!HopliteSession.isActive()) {
            return;
        }
        boolean send = shouldSend(title);
        if (!send || !config.enabled || !config.autoDamageSummary) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) {
            sentForRoundTwo = false;
            return;
        }
        client.getConnection().sendCommand("damagesummary");
    }

    static boolean shouldSend(Component title) {
        if (title == null) {
            return false;
        }
        String text = title.getString().toLowerCase(Locale.ROOT);
        if (ROUND_ONE.matcher(text).find()) {
            sentForRoundTwo = false;
            return false;
        }
        if (!ROUND_TWO.matcher(text).find() || sentForRoundTwo) {
            return false;
        }
        sentForRoundTwo = true;
        return true;
    }

    static void reset() {
        sentForRoundTwo = false;
    }
}
