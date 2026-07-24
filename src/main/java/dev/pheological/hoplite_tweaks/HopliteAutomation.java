package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.sounds.SoundEvents;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Small, independently gated quality-of-life automations for Hoplite.
 */
public final class HopliteAutomation {
    private static final ZoneId PACIFIC = ZoneId.of("America/Los_Angeles");
    private static long joinedAt;
    private static long lastPartyCommand;
    private static long lastPet;
    private static long lastHandledMessage;
    private static String lastMessage = "";
    private static boolean crateCheckedThisSession;

    private HopliteAutomation() {
    }

    public static void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            joinedAt = System.currentTimeMillis();
            crateCheckedThisSession = false;
            lastMessage = "";
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            joinedAt = 0;
            crateCheckedThisSession = false;
            lastMessage = "";
        });

        ClientReceiveMessageEvents.GAME.register(HopliteAutomation::onGameMessage);
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, receivedAt) ->
            onChatMessage(message)
        );
        ClientTickEvents.END_CLIENT_TICK.register(HopliteAutomation::tick);
    }

    private static void onGameMessage(Component message, boolean overlay) {
        if (!isEnabled()) {
            return;
        }
        if (overlay) {
            tryAutoPet(message);
        } else {
            onChatMessage(message);
        }
    }

    private static void onChatMessage(Component message) {
        if (!isEnabled()) {
            return;
        }

        String plain = message.getString().trim();
        long now = System.currentTimeMillis();
        if (plain.equals(lastMessage) && now - lastHandledMessage < 250) {
            return;
        }
        lastMessage = plain;
        lastHandledMessage = now;

        Minecraft client = Minecraft.getInstance();
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        String lower = plain.toLowerCase(Locale.ROOT);
        String playerName = client.player == null
            ? ""
            : client.player.getGameProfile().name().toLowerCase(Locale.ROOT);

        boolean partyMessage = hasStyledText(message, "party", HopliteAutomation::isBlue);
        boolean mentioned = !playerName.isBlank() && lower.contains(playerName);
        boolean sentByPlayer = !playerName.isBlank() && lower.contains(playerName + ":");
        if (config.partyMessagePing && !sentByPlayer && (partyMessage || mentioned)) {
            client.getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.25F)
            );
        }

        if (config.autoPartyChat
            && now - lastPartyCommand >= 10_000
            && hasStyledText(message, "joined", HopliteAutomation::isLightGreen)
            && client.getConnection() != null) {
            client.getConnection().sendCommand("party chat");
            lastPartyCommand = now;
        }
    }

    private static void tryAutoPet(Component message) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        Minecraft client = Minecraft.getInstance();
        if (!config.autoPet
            || client.player == null
            || client.getConnection() == null
            || !client.player.getInventory().isEmpty()
            || client.player.getVehicle() == null) {
            return;
        }

        String text = message.getString().trim();
        boolean battleBusPrompt = text.startsWith("Time to drop:")
            || text.startsWith("Players can drop in ")
            || text.equals("SNEAK or JUMP to drop");
        long now = System.currentTimeMillis();
        if (battleBusPrompt && now - lastPet >= 30_000) {
            client.getConnection().getConnection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.DROP_ITEM,
                BlockPos.ZERO,
                Direction.DOWN
            ));
            lastPet = now;
        }
    }

    private static void tick(Minecraft client) {
        if (crateCheckedThisSession
            || joinedAt == 0
            || System.currentTimeMillis() - joinedAt < 5_000
            || !isEnabled()
            || !HopliteTweaksConfig.get().weeklyCrateReminder
            || client.player == null) {
            return;
        }
        crateCheckedThisSession = true;

        ZonedDateTime pacificNow = ZonedDateTime.ofInstant(Instant.now(), PACIFIC);
        if (pacificNow.getHour() < 1) {
            return;
        }

        WeekFields weeks = WeekFields.ISO;
        String week = pacificNow.get(weeks.weekBasedYear())
            + "-"
            + pacificNow.get(weeks.weekOfWeekBasedYear());
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (week.equals(config.lastCrateReminderWeek)) {
            return;
        }

        Component reminder = Component.literal("[Hoplite Tweaks] ")
            .withStyle(ChatFormatting.AQUA)
            .append(Component.literal("Your weekly crate may be ready—remember to collect it!")
                .withStyle(ChatFormatting.GOLD));
        HopliteChat.send(reminder);
        client.getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.9F)
        );
        config.lastCrateReminderWeek = week;
        HopliteTweaksConfig.save();
    }

    private static boolean hasStyledText(
        Component message,
        String needle,
        java.util.function.IntPredicate colorPredicate
    ) {
        String lowerNeedle = needle.toLowerCase(Locale.ROOT);
        return message.toFlatList().stream().anyMatch(part -> {
            TextColor color = part.getStyle().getColor();
            return color != null
                && part.getString().toLowerCase(Locale.ROOT).contains(lowerNeedle)
                && colorPredicate.test(color.getValue());
        });
    }

    private static boolean isBlue(int color) {
        int red = color >>> 16 & 0xFF;
        int green = color >>> 8 & 0xFF;
        int blue = color & 0xFF;
        return blue >= 150 && (blue > red * 1.15F || green >= 140);
    }

    private static boolean isLightGreen(int color) {
        int red = color >>> 16 & 0xFF;
        int green = color >>> 8 & 0xFF;
        int blue = color & 0xFF;
        return green >= 180 && green > red * 1.15F && green > blue * 1.10F;
    }

    private static boolean isEnabled() {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        return HopliteSession.isActive() && config.enabled;
    }
}
