package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Spaces normal chat messages for unranked players without delaying commands.
 */
public final class MessageDelay {
    private static final long DELAY_MILLIS = 3_000;
    private static final Queue<String> QUEUE = new ArrayDeque<>();
    private static long nextSendAt;
    private static boolean sendingQueuedMessage;

    private MessageDelay() {
    }

    public static void initialize() {
        ClientSendMessageEvents.ALLOW_CHAT.register(MessageDelay::queueIfNeeded);
        ClientTickEvents.END_CLIENT_TICK.register(MessageDelay::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
    }

    private static boolean queueIfNeeded(String message) {
        if (sendingQueuedMessage || !shouldDelay(Minecraft.getInstance())) {
            return true;
        }
        QUEUE.add(message);
        return false;
    }

    private static void tick(Minecraft client) {
        if (QUEUE.isEmpty() || client.getConnection() == null) {
            return;
        }

        if (!shouldDelay(client)) {
            sendNext(client, false);
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= nextSendAt) {
            sendNext(client, true);
            nextSendAt = now + DELAY_MILLIS;
        }
    }

    private static void sendNext(Minecraft client, boolean delayed) {
        String message = QUEUE.poll();
        if (message == null) {
            return;
        }
        sendingQueuedMessage = true;
        try {
            client.getConnection().sendChat(message);
        } finally {
            sendingQueuedMessage = false;
        }
        if (!delayed) {
            nextSendAt = 0;
        }
    }

    private static boolean shouldDelay(Minecraft client) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        return config.enabled
            && config.messageDelay
            && HopliteSession.isActive()
            && client.player != null
            && NonRankDetector.isNon(client.player.getGameProfile().name());
    }

    private static void clear() {
        QUEUE.clear();
        nextSendAt = 0;
        sendingQueuedMessage = false;
    }
}
