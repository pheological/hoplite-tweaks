package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HopliteHudTest {
    @Test
    void sharedItemUsesCooldownThatEndsLast() {
        long now = 10_000;
        ApolloModels.Cooldown longCooldown = new ApolloModels.Cooldown(
            "Primary", "minecraft:diamond_sword", 0, 20_000
        );
        ApolloModels.Cooldown shortRecentCooldown = new ApolloModels.Cooldown(
            "Secondary", "minecraft:diamond_sword", 9_000, 5_000
        );

        assertEquals(
            0.5F,
            HopliteHud.hotbarRemaining(
                List.of(shortRecentCooldown, longCooldown),
                "minecraft:diamond_sword",
                now
            )
        );
    }

    @Test
    void normalizesFormattedItemNamesForCooldownMatching() {
        assertEquals(
            "emerald blade level 2",
            HopliteHud.normalizeName("\u00A7aEmerald Blade \u00A77[Level 2]")
        );
    }
}
