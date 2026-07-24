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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public final class HopliteHud {
    private static final int COOLDOWN_BACKGROUND = 0xDD111721;
    private static final int COOLDOWN_BORDER = 0xA037465C;
    private static final int COOLDOWN_SHADOW = 0x66000000;
    private static final int PROGRESS_TRACK = 0xB02B3546;
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
        if (config.showCooldownsInHotbar) {
            renderHotbarCooldowns(graphics, client);
        }
        if (config.showCooldownsAtTop) {
            renderTopCooldownBars(graphics, client);
        }
    }

    private static void renderHotbarCooldowns(Object graphics, Minecraft client) {
        List<ApolloModels.Cooldown> cooldowns = ApolloState.cooldowns().stream().toList();
        if (cooldowns.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        int firstSlotX = guiWidth(graphics) / 2 - 88;
        int slotY = guiHeight(graphics) - 19;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            float remaining = hotbarRemaining(cooldowns, itemId, now);
            if (remaining <= 0.0F) {
                continue;
            }

            int x = firstSlotX + slot * 20;
            int overlayTop = slotY + (int) Math.floor(16.0F * (1.0F - remaining));
            int overlayBottom = overlayTop + (int) Math.ceil(16.0F * remaining);
            fill(graphics, x, overlayTop, x + 16, overlayBottom, 0x7FFFFFFF);
        }
    }

    static float hotbarRemaining(
        List<ApolloModels.Cooldown> cooldowns,
        String itemId,
        long now
    ) {
        return cooldowns.stream()
            .filter(cooldown -> itemId.equals(cooldown.itemId()))
            .filter(cooldown -> cooldown.remainingMillis(now) > 0)
            .max(Comparator.comparingLong(cooldown ->
                cooldown.startedAt() + cooldown.durationMillis()
            ))
            .map(cooldown -> 1.0F - cooldown.progress(now))
            .orElse(0.0F);
    }

    private static void renderTopCooldownBars(Object graphics, Minecraft client) {
        long now = System.currentTimeMillis();
        int maxBars = Math.max(1, guiHeight(graphics) / 54);
        List<ApolloModels.Cooldown> cooldowns = ApolloState.cooldowns().stream()
            .sorted(Comparator.comparingLong(cooldown -> cooldown.remainingMillis(now)))
            .limit(maxBars)
            .toList();
        if (cooldowns.isEmpty()) {
            return;
        }

        int width = 182;
        int x = (guiWidth(graphics) - width) / 2;
        int y = 8;
        for (ApolloModels.Cooldown cooldown : cooldowns) {
            float remainingProgress = 1.0F - cooldown.progress(now);
            int accent = progressColor(cooldown.progress(now));
            String remaining = timeText(cooldown.remainingMillis(now));
            int textX = (guiWidth(graphics) - client.font.width(remaining)) / 2;
            ItemStack matchingItem = findHotbarItem(client, cooldown.name());

            if (!matchingItem.isEmpty()) {
                item(graphics, matchingItem, x - 20, y + 1);
            }
            text(graphics, client.font, remaining, textX, y, WHITE);
            roundedFill(graphics, x + 1, y + 11, width, 7, COOLDOWN_SHADOW);
            roundedFill(graphics, x, y + 10, width, 7, COOLDOWN_BORDER);
            roundedFill(graphics, x + 1, y + 11, width - 2, 5, PROGRESS_TRACK);
            int fillWidth = Math.round((width - 2) * remainingProgress);
            if (fillWidth > 0) {
                roundedFill(graphics, x + 1, y + 11, fillWidth, 5, accent);
            }
            y += 21;
        }
    }

    private static ItemStack findHotbarItem(Minecraft client, String cooldownName) {
        String normalizedCooldown = normalizeName(cooldownName);
        if (normalizedCooldown.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack partialMatch = ItemStack.EMPTY;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            String normalizedItem = normalizeName(stack.getHoverName().getString());
            if (normalizedCooldown.equals(normalizedItem)) {
                return stack;
            }
            if (partialMatch.isEmpty()
                && normalizedItem.length() >= 3
                && (normalizedCooldown.contains(normalizedItem)
                    || normalizedItem.contains(normalizedCooldown))) {
                partialMatch = stack;
            }
        }
        return partialMatch;
    }

    static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        StringBuilder normalized = new StringBuilder(value.length());
        boolean pendingSpace = false;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '\u00A7' && index + 1 < value.length()) {
                index++;
                continue;
            }
            if (Character.isLetterOrDigit(character)) {
                if (pendingSpace && !normalized.isEmpty()) {
                    normalized.append(' ');
                }
                normalized.append(Character.toLowerCase(character));
                pendingSpace = false;
            } else {
                pendingSpace = true;
            }
        }
        return normalized.toString();
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

        int width = Math.round((compact ? 100 : 126) * scale);
        int rowHeight = Math.round((compact ? 17 : 21) * scale);
        int gap = Math.max(1, Math.round(2 * scale));
        int height = cooldowns.size() * rowHeight + Math.max(0, cooldowns.size() - 1) * gap;
        int x = Math.round(Math.max(0, guiWidth(graphics) - width) * config.hudXPercent / 100.0F);
        int y = Math.round(Math.max(0, guiHeight(graphics) - height) * config.hudYPercent / 100.0F);

        long now = System.currentTimeMillis();
        int rowY = y;
        for (ApolloModels.Cooldown cooldown : cooldowns) {
            float progress = cooldown.progress(now);
            long remaining = cooldown.remainingMillis(now);
            int accent = progressColor(progress);

            roundedFill(graphics, x + 1, rowY + 2, width, rowHeight, COOLDOWN_SHADOW);
            roundedFill(graphics, x, rowY, width, rowHeight, COOLDOWN_BORDER);
            roundedFill(graphics, x + 1, rowY + 1, width - 2, rowHeight - 2, COOLDOWN_BACKGROUND);
            fill(graphics, x + 4, rowY + 1, x + width - 4, rowY + 2, 0x20FFFFFF);

            int textY = rowY + Math.max(3, Math.round((rowHeight - 9) / 2.0F));
            text(graphics, font, abbreviate(cooldown.name(), compact ? 11 : 15), x + 6, textY, WHITE);
            String time = timeText(remaining);
            int badgeWidth = font.width(time) + 8;
            int badgeX = x + width - badgeWidth - 3;
            roundedFill(graphics, badgeX, rowY + 3, badgeWidth, rowHeight - 7,
                withAlpha(accent, 0x2E));
            text(graphics, font, time, badgeX + 4, textY, accent);

            int trackWidth = width - 10;
            int progressWidth = Math.round(trackWidth * (1.0F - progress));
            fill(graphics, x + 5, rowY + rowHeight - 3, x + width - 5, rowY + rowHeight - 1,
                PROGRESS_TRACK);
            fill(graphics, x + 5, rowY + rowHeight - 3, x + 5 + progressWidth,
                rowY + rowHeight - 1, accent);
            rowY += rowHeight + gap;
        }
        return y + height;
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

    private static void roundedFill(
        Object graphics,
        int x,
        int y,
        int width,
        int height,
        int color
    ) {
        if (width <= 2 || height <= 2) {
            fill(graphics, x, y, x + width, y + height, color);
            return;
        }
        fill(graphics, x + 1, y, x + width - 1, y + height, color);
        fill(graphics, x, y + 1, x + width, y + height - 1, color);
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

    private static void item(Object graphics, ItemStack stack, int x, int y) {
        //? >=26 {
        /*((GuiGraphicsExtractor) graphics).item(stack, x, y);
        *///?} else {
        ((GuiGraphics) graphics).renderItem(stack, x, y);
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

    private static int withAlpha(int color, int alpha) {
        return alpha << 24 | color & 0x00FFFFFF;
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
