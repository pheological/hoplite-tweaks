package dev.pheological.hoplite_tweaks;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Learns whether a player has no rank from the color of their name in chat.
 */
public final class NonRankDetector {
    private static final Map<String, Boolean> NON_STATUS = new ConcurrentHashMap<>();

    private NonRankDetector() {
    }

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                observe(message);
            }
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, receivedAt) ->
            observe(message)
        );
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> NON_STATUS.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> NON_STATUS.clear());
    }

    private static void observe(Component message) {
        String plain = message.getString();
        int colon = plain.indexOf(':');
        if (colon < 1) {
            return;
        }

        String prefix = plain.substring(0, colon);
        for (String token : prefix.split("[^A-Za-z0-9_]+")) {
            if (!token.isEmpty()) {
                Boolean status = nonStatus(message, token);
                if (status != null) {
                    NON_STATUS.put(token.toLowerCase(Locale.ROOT), status);
                }
            }
        }
    }

    /**
     * Returns whether the latest chat line from {@code playerName} showed a gray sender name.
     */
    public static boolean isNon(String playerName) {
        if (playerName == null) {
            return false;
        }
        return NON_STATUS.getOrDefault(playerName.toLowerCase(Locale.ROOT), false);
    }

    /**
     * Checks whether the named sender before the first colon is gray.
     */
    public static boolean isNon(Component message, String playerName) {
        return Boolean.TRUE.equals(nonStatus(message, playerName));
    }

    private static Boolean nonStatus(Component message, String playerName) {
        if (message == null || playerName == null || playerName.isBlank()) {
            return null;
        }

        String plain = message.getString();
        int colon = plain.indexOf(':');
        if (colon < 1) {
            return null;
        }

        String lowerPrefix = plain.substring(0, colon).toLowerCase(Locale.ROOT);
        String lowerName = playerName.toLowerCase(Locale.ROOT);
        int nameStart = lowerPrefix.lastIndexOf(lowerName);
        if (nameStart < 0 || !hasUsernameBoundaries(lowerPrefix, nameStart, lowerName.length())) {
            return null;
        }

        int nameEnd = nameStart + lowerName.length();
        int offset = 0;
        boolean foundColoredCharacter = false;
        for (Component part : message.toFlatList()) {
            String text = part.getString();
            int partEnd = offset + text.length();
            int overlapStart = Math.max(nameStart, offset);
            int overlapEnd = Math.min(nameEnd, partEnd);
            if (overlapStart < overlapEnd) {
                TextColor color = part.getStyle().getColor();
                if (color == null || !isGray(color.getValue())) {
                    return false;
                }
                foundColoredCharacter = true;
            }
            offset = partEnd;
            if (offset >= nameEnd) {
                break;
            }
        }
        return foundColoredCharacter;
    }

    private static boolean hasUsernameBoundaries(String value, int start, int length) {
        int end = start + length;
        return (start == 0 || !isUsernameCharacter(value.charAt(start - 1)))
            && (end == value.length() || !isUsernameCharacter(value.charAt(end)));
    }

    private static boolean isUsernameCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }

    private static boolean isGray(int color) {
        int red = color >>> 16 & 0xFF;
        int green = color >>> 8 & 0xFF;
        int blue = color & 0xFF;
        int brightest = Math.max(red, Math.max(green, blue));
        int darkest = Math.min(red, Math.min(green, blue));
        return brightest - darkest <= 12 && brightest >= 70 && brightest <= 210;
    }
}
