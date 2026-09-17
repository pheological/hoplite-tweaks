package dev.pheological.hoplite_tweaks;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoDamageSummaryTest {
    @AfterEach
    void reset() {
        AutoDamageSummary.reset();
    }

    @Test
    void sendsOnceForRepeatedRoundTwoTitles() {
        assertTrue(AutoDamageSummary.shouldSend(Component.literal("Round 2")));
        assertFalse(AutoDamageSummary.shouldSend(Component.literal("ROUND 2")));
        assertFalse(AutoDamageSummary.shouldSend(Component.literal("Prepare for round 2!")));
    }

    @Test
    void roundOneArmsTheNextRoundTwo() {
        assertTrue(AutoDamageSummary.shouldSend(Component.literal("Round 2")));
        assertFalse(AutoDamageSummary.shouldSend(Component.literal("Round 1")));
        assertTrue(AutoDamageSummary.shouldSend(Component.literal("Starting Round 2 now")));
    }

    @Test
    void ignoresUnrelatedRoundsAndPartialNumbers() {
        assertFalse(AutoDamageSummary.shouldSend(Component.literal("Round 20")));
        assertFalse(AutoDamageSummary.shouldSend(Component.literal("Round 12")));
        assertFalse(AutoDamageSummary.shouldSend(Component.literal("Victory")));
        assertFalse(AutoDamageSummary.shouldSend(null));
    }
}
