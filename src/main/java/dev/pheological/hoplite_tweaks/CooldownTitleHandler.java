package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Clears stale cooldowns when Hoplite announces a new round or match result.
 */
public final class CooldownTitleHandler {
    private CooldownTitleHandler() {
    }

    public static void onTitle(Component title) {
        if (HopliteSession.isActive() && shouldClear(title)) {
            ApolloState.clearCooldowns();
        }
    }

    static boolean shouldClear(Component title) {
        if (title == null) {
            return false;
        }
        String text = title.getString().toLowerCase(Locale.ROOT);
        return text.contains("round") || text.contains("victory") || text.contains("defeat");
    }
}
