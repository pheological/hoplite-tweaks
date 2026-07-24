package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
//? >=26 {
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
*///?} else {
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

public final class HopliteHud {
    private static final int PANEL = 0xCC10141D;
    private static final int PANEL_EDGE = 0xFF263245;
    private static final int MUTED = 0xFF9AA7B8;
    private static final int WHITE = 0xFFF4F7FB;

    private HopliteHud() {
    }

    public static void initialize() {
        //? >=26 {
        /*HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "main_hud"),
            HopliteHud::render
        );
        *///?} else {
        HudRenderCallback.EVENT.register(HopliteHud::render);
        //?}
    }

    //? >=26 {
    /*private static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker tickCounter) {
    *///?} else {
    private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker tickCounter) {
    //?}
        Minecraft client = Minecraft.getInstance();
        if (!HopliteSession.isActive()
            || !HopliteTweaksConfig.get().enabled
            || client.player == null
            || isGuiHidden(client)) {
            return;
        }

        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        float scale = config.hudScalePercent / 100.0F;

        if (config.cooldownHud) {
            renderCooldowns(graphics, client.font, scale, config);
        }
    }

    private static int renderCooldowns(
        Object graphics,
        Font font,
        float scale,
        HopliteTweaksConfig config
    ) {
        boolean compact = config.compactCooldowns;
        List<ApolloModels.Cooldown> cooldowns = ApolloState.cooldowns().stream()
            .sorted(Comparator.comparingLong(ApolloModels.Cooldown::durationMillis))
            .toList();
        if (cooldowns.isEmpty()) {
            return 0;
        }

        int width = Math.round((compact ? 112 : 148) * scale);
        int rowHeight = Math.round((compact ? 23 : 30) * scale);
        int header = Math.round(21 * scale);
        int height = header + cooldowns.size() * rowHeight + Math.round(5 * scale);
        int x = Math.round(Math.max(0, guiWidth(graphics) - width) * config.hudXPercent / 100.0F);
        int y = Math.round(Math.max(0, guiHeight(graphics) - height) * config.hudYPercent / 100.0F);
        panel(graphics, x, y, width, height);
        text(graphics, font, Component.literal("COOLDOWNS"), x + 8, y + 7, MUTED);

        long now = System.currentTimeMillis();
        int rowY = y + header;
        for (ApolloModels.Cooldown cooldown : cooldowns) {
            float progress = cooldown.progress(now);
            long remaining = cooldown.remainingMillis(now);
            int accent = progressColor(progress);
            int innerX = x + Math.round(7 * scale);
            int innerWidth = width - Math.round(14 * scale);

            fill(graphics, innerX, rowY, innerX + innerWidth, rowY + rowHeight - 3, 0xB819202C);
            fill(graphics, innerX, rowY, innerX + Math.round(3 * scale), rowY + rowHeight - 3, accent);
            text(graphics, font, abbreviate(cooldown.name(), compact ? 13 : 19), innerX + 7, rowY + 4, WHITE);
            text(graphics, font, timeText(remaining), innerX + innerWidth - 28, rowY + 4, accent);

            int barY = rowY + rowHeight - 7;
            fill(graphics, innerX + 7, barY, innerX + innerWidth - 5, barY + 2, 0xFF303B4D);
            int available = innerWidth - 12;
            fill(graphics, innerX + 7, barY, innerX + 7 + Math.round(available * (1.0F - progress)), barY + 2, accent);
            rowY += rowHeight;
        }
        return y + height;
    }

    private static void panel(Object graphics, int x, int y, int width, int height) {
        fill(graphics, x, y, x + width, y + height, PANEL);
        fill(graphics, x, y, x + width, y + 1, PANEL_EDGE);
        fill(graphics, x, y + height - 1, x + width, y + height, PANEL_EDGE);
        fill(graphics, x, y, x + 1, y + height, PANEL_EDGE);
        fill(graphics, x + width - 1, y, x + width, y + height, PANEL_EDGE);
    }

    private static int guiWidth(Object graphics) {
        //? >=26 {
        /*return ((GuiGraphicsExtractor) graphics).guiWidth();
        *///?} else {
        return ((GuiGraphics) graphics).guiWidth();
        //?}
    }

    private static int guiHeight(Object graphics) {
        //? >=26 {
        /*return ((GuiGraphicsExtractor) graphics).guiHeight();
        *///?} else {
        return ((GuiGraphics) graphics).guiHeight();
        //?}
    }

    private static void fill(Object graphics, int left, int top, int right, int bottom, int color) {
        //? >=26 {
        /*((GuiGraphicsExtractor) graphics).fill(left, top, right, bottom, color);
        *///?} else {
        ((GuiGraphics) graphics).fill(left, top, right, bottom, color);
        //?}
    }

    private static void text(Object graphics, Font font, Component value, int x, int y, int color) {
        //? >=26 {
        /*((GuiGraphicsExtractor) graphics).text(font, value, x, y, color, false);
        *///?} else {
        ((GuiGraphics) graphics).drawString(font, value, x, y, color, false);
        //?}
    }

    private static void text(Object graphics, Font font, String value, int x, int y, int color) {
        //? >=26 {
        /*((GuiGraphicsExtractor) graphics).text(font, value, x, y, color, false);
        *///?} else {
        ((GuiGraphics) graphics).drawString(font, value, x, y, color, false);
        //?}
    }

    private static int progressColor(float progress) {
        if (progress < 0.5F) {
            return 0xFFFF6B7A;
        }
        if (progress < 0.8F) {
            return 0xFFFFCA62;
        }
        return 0xFF54D6A2;
    }

    private static boolean isGuiHidden(Minecraft client) {
        //? >=26.2 {
        /*return false;
        *///?} else {
        return client.options.hideGui;
        //?}
    }

    private static String timeText(long millis) {
        if (millis >= 10_000) {
            return (millis + 999) / 1000 + "s";
        }
        return String.format(java.util.Locale.ROOT, "%.1f", millis / 1000.0);
    }

    private static String abbreviate(String value, int max) {
        if (value == null || value.isBlank()) {
            return "Cooldown";
        }
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 1)) + "…";
    }

}
