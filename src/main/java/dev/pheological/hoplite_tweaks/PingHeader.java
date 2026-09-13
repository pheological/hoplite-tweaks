package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.UUID;

/** Builds the client-side latency label used by player nametags. */
public final class PingHeader {
    private static final int[] LATENCY_STOPS = {0, 50, 100, 150, 200, 300};
    private static final int[] COLOR_STOPS = {
        0x008000, 0x55FF55, 0xFFFF55, 0xFFAA00, 0xFF5555, 0xAA0000
    };

    private PingHeader() {}

    public static Component counter(UUID playerId) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        Minecraft client = Minecraft.getInstance();
        if (!config.enabled || !config.pingHeader || playerId == null
            || client.getCurrentServer() == null || client.getConnection() == null) {
            return null;
        }
        PlayerInfo info = client.getConnection().getPlayerInfo(playerId);
        return info == null ? null : label(info.getLatency(), config.pingLeftText, config.pingRightText);
    }

    static Component label(int latency, String left, String right) {
        if (latency < 0) return null;
        String prefix = left == null ? "" : left;
        String suffix = right == null ? "" : right;
        return Component.literal(prefix + latency + suffix).setStyle(Style.EMPTY.withColor(color(latency))
            .withBold(false).withItalic(false).withUnderlined(false)
            .withStrikethrough(false).withObfuscated(false));
    }

    static int color(int latency) {
        int value = Math.max(0, latency);
        for (int i = 1; i < LATENCY_STOPS.length; i++) {
            if (value <= LATENCY_STOPS[i]) {
                int start = LATENCY_STOPS[i - 1], end = LATENCY_STOPS[i];
                float progress = (value - start) / (float) (end - start);
                return interpolate(COLOR_STOPS[i - 1], COLOR_STOPS[i], progress);
            }
        }
        return COLOR_STOPS[COLOR_STOPS.length - 1];
    }

    private static int interpolate(int start, int end, float progress) {
        int red = Math.round(((start >> 16) & 0xFF) + (((end >> 16) & 0xFF) - ((start >> 16) & 0xFF)) * progress);
        int green = Math.round(((start >> 8) & 0xFF) + (((end >> 8) & 0xFF) - ((start >> 8) & 0xFF)) * progress);
        int blue = Math.round((start & 0xFF) + ((end & 0xFF) - (start & 0xFF)) * progress);
        return (red << 16) | (green << 8) | blue;
    }
}
