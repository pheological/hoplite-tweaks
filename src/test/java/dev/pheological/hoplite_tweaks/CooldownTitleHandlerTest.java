package dev.pheological.hoplite_tweaks;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownTitleHandlerTest {
    @Test
    void recognizesRoundAndMatchResultTitlesAnywhereInText() {
        assertTrue(CooldownTitleHandler.shouldClear(Component.literal("Round 2")));
        assertTrue(CooldownTitleHandler.shouldClear(Component.literal("Flawless VICTORY!")));
        assertTrue(CooldownTitleHandler.shouldClear(Component.literal("A crushing defeat")));
    }

    @Test
    void leavesUnrelatedTitlesAlone() {
        assertFalse(CooldownTitleHandler.shouldClear(Component.literal("The gates open")));
        assertFalse(CooldownTitleHandler.shouldClear(null));
    }
}
