package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class KillCounterStateTest {
    private final UUID victim = UUID.randomUUID(), attacker = UUID.randomUUID();
    private final Object world = new Object();
    private KillCounterState state() {
        var state = new KillCounterState();
        state.update(world, true, List.of("§eGAME STATS"), 0, false);
        state.observe("_Victim1", victim);
        state.observe("9Attacker_", attacker);
        return state;
    }

    @Test void verbsFormattingAndUsernameCharacters() {
        var state = state();
        for (String verb : List.of("slain", "dazzled", "sent to another dimension", "wasp-stung")) {
            assertEquals(new KillCounterState.Kill(victim, attacker), state.parse(
                "⚔ §c_Victim1§r was " + verb + " by §a9Attacker_§r ⚑"));
        }
        assertEquals(new KillCounterState.Kill(victim, attacker), state.parse(
            "ELIMINATION! ☠ _VICTIM1 was dazzled by ⚔ 9ATTACKER_ (FINAL KILL)"));
        assertNull(state.parse("_Victim1 wasp dazzled by 9Attacker_"));
        assertNull(state.parse("_Victim1 was killed nearby 9Attacker_"));
        assertNull(state.parse("_Victim1 by killed was 9Attacker_"));
        assertNull(state.parse("_Victim1 was by 9Attacker_"));
        assertNull(state.parse(null));
    }

    @Test void prefersValidatedEndpointsThenFallsBack() {
        var state = state();
        var other = UUID.randomUUID();
        state.observe("Other", other);
        assertEquals(new KillCounterState.Kill(other, attacker),
            state.parse("Other _Victim1 was slain by Other 9Attacker_"));
        assertEquals(new KillCounterState.Kill(victim, attacker),
            state.parse("ELIMINATION _Victim1 was slain by 9Attacker_ FINAL"));
        assertNull(state.parse("Missing was slain by 9Attacker_"));
        assertNull(state.parse("_Victim1 was slain by _Victim1"));
        state.observe("_victim1", UUID.randomUUID());
        assertNull(state.parse("_Victim1 was slain by 9Attacker_"));
    }

    @Test void duplicatesExpireWithoutExtendingWindowAndDepartedProfilesRemain() {
        var state = state();
        assertTrue(state.accept("⚔ _Victim1 was slain by 9Attacker_", 0));
        assertFalse(state.accept("§c_VICTIM1  was slain by 9Attacker_!", 1999));
        assertTrue(state.accept("_Victim1 was slain by 9Attacker_", 2000));
        // No longer in the current roster, but retained from this match.
        state.observe("9Attacker_", attacker);
        assertTrue(state.accept("_Victim1 was dazzled by 9Attacker_", 2001));
        assertEquals(3, state.count(attacker));
        assertEquals(0, state.count(victim));
    }

    @Test void pointedDripstoneKillCreditsAttackerAndAwardsOneBadge() {
        var state = state();
        String message = "§c_Victim1§r was pricked to death by a Pointed Dripstone while fighting §a9Attacker_§r";

        assertTrue(state.accept(message, 0));
        assertEquals(1, state.count(attacker));
        assertTrue(state.hasDripstoneBadge(attacker));
        assertFalse(state.hasDripstoneBadge(victim));

        assertFalse(state.accept("☠ _Victim1 was pricked to death by a Pointed Dripstone while fighting 9Attacker_", 1));
        assertEquals(1, state.count(attacker));
        assertTrue(state.hasDripstoneBadge(attacker));
    }

    @Test void playerChatAndColonFormattedMessagesCannotSpoofKillsOrBadges() {
        var state = state();
        String death = "_Victim1 was pricked to death by a Pointed Dripstone while fighting 9Attacker_";

        assertFalse(state.accept(death, 0, true));
        assertFalse(state.accept("Player: " + death, 1, false));
        assertEquals(0, state.count(attacker));
        assertFalse(state.hasDripstoneBadge(attacker));
    }

    @Test void dripstoneBadgeRequiresExactCauseAndFinalRecognizedAttacker() {
        for (String message : List.of(
            "_Victim1 was pricked by a Pointed Dripstone while fighting 9Attacker_",
            "_Victim1 was pricked to death by Pointed Dripstone while fighting 9Attacker_",
            "_Victim1 was pricked to death while fighting 9Attacker_ by a Pointed Dripstone",
            "_Victim1 was pricked to death by a Pointed Dripstone while fighting Missing",
            "_Victim1 was pricked to death by a Pointed Dripstone while fighting 9Attacker_ nearby"
        )) {
            var state = state();
            state.accept(message, 0);
            assertFalse(state.hasDripstoneBadge(attacker), message);
        }
    }

    @Test void ordinaryServerKillStillCountsWithoutBadge() {
        var state = state();
        assertTrue(state.accept("_Victim1 was dazzled by 9Attacker_", 0, false));
        assertEquals(1, state.count(attacker));
        assertFalse(state.hasDripstoneBadge(attacker));
    }

    @Test void optionalModeStartsTrackingWhenMiningPhaseLeavesScoreboard() {
        var state = new KillCounterState();
        state.update(world, true, List.of("SOLO ROYALE", "Your Stats:", "Mining Phase in:", "4 minutes"), 0, true);
        state.observe("_Victim1", victim);
        state.observe("9Attacker_", attacker);

        assertTrue(state.inGame());
        assertFalse(state.trackingKills());
        assertFalse(state.accept("_Victim1 was slain by 9Attacker_", 1));
        assertEquals(0, state.count(attacker));

        state.update(world, true, List.of("SOLO ROYALE", "Your Stats:", "Border: +1,000"), 2, true);
        assertTrue(state.trackingKills());
        assertTrue(state.accept("_Victim1 was slain by 9Attacker_", 3));
        assertEquals(1, state.count(attacker));
    }

    @Test void dimensionTransitionWaitsAndPreservesMatch() {
        var state = state();
        state.accept("_Victim1 was slain by 9Attacker_", 0);
        Object dimension = new Object();
        state.update(dimension, true, null, 100, false);
        assertFalse(state.inGame());
        assertFalse(state.accept("_Victim1 was dazzled by 9Attacker_", 101));
        state.update(dimension, true, null, 5099, false);
        assertEquals(1, state.count(attacker));
        state.update(dimension, true, List.of("Hoplite", "Game Stats", "Kills: 1"), 5100, false);
        assertTrue(state.inGame());
        assertEquals(1, state.count(attacker));
    }

    @Test void missingSidebarClearsAtFiveSecondsAndLobbyClearsImmediately() {
        var state = state();
        state.accept("_Victim1 was slain by 9Attacker_", 0);
        Object dimension = new Object();
        state.update(dimension, true, null, 10, false);
        state.update(dimension, true, null, 5010, false);
        assertEquals(0, state.count(attacker));
        assertNull(state.resolve("_Victim1"));
        state = state();
        state.accept("_Victim1 was slain by 9Attacker_", 0);
        state.update(new Object(), true, List.of("Lobby"), 10, false);
        assertEquals(0, state.count(attacker));
        assertFalse(state.inGame());
    }

    @Test void disconnectAndNonHopliteClearAndResetDoesNotStopMatch() {
        var state = state();
        state.accept("_Victim1 was pricked to death by a Pointed Dripstone while fighting 9Attacker_", 0);
        assertTrue(state.hasDripstoneBadge(attacker));
        state.resetCounts();
        assertTrue(state.inGame());
        assertEquals(0, state.count(attacker));
        assertFalse(state.hasDripstoneBadge(attacker));
        state.clear();
        assertFalse(state.inGame());
        state = state();
        state.accept("_Victim1 was slain by 9Attacker_", 0);
        state.update(world, false, List.of("Game Stats"), 1, false);
        assertFalse(state.inGame());
        assertEquals(0, state.count(attacker));
    }

    @Test void sidebarRequiresOneWholeRecognizedName() {
        var state = state();
        assertEquals(attacker, state.playerInRow("[VIP] §e9Attacker_ : 12"));
        assertNull(state.playerInRow("Game Stats"));
        assertNull(state.playerInRow("Kills: 3"));
        assertNull(state.playerInRow("9Attacker_suffix"));
        assertNull(state.playerInRow("_Victim1 and 9Attacker_"));
        state.observe("9Attacker_", UUID.randomUUID());
        assertNull(state.playerInRow("9Attacker_"));
    }
}
