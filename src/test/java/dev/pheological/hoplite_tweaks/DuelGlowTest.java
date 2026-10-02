package dev.pheological.hoplite_tweaks;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuelGlowTest {
    @Test
    void lobbyIdentifiersNeedMatchTimer() {
        for (int id = 1; id <= 10; id++) {
            String marker = "09/30/26 00:04 NA D" + id;
            assertFalse(DuelGlow.isDuelMatch(List.of(marker, "Division: MASTER", "Total Wins: 4,112")));
            assertTrue(DuelGlow.isDuelMatch(List.of(marker, "Time Left: 00:30")));
        }
    }

    @Test
    void requiresBothDuelMarkerAndTimeLeft() {
        assertFalse(DuelGlow.isDuelMatch(List.of("NA D1", "Time: 00:30")));
        assertFalse(DuelGlow.isDuelMatch(List.of("NA BR1", "Time Left: 00:30")));
        assertFalse(DuelGlow.isDuelMatch(List.of("Opponent: Player")));
        assertFalse(DuelGlow.isDuelMatch(List.of()));
        assertTrue(DuelGlow.isDuelMatch(List.of("NA D", "Time Left: 00:30")));
        assertTrue(DuelGlow.isDuelMatch(List.of("NA D11", "Time Left: 00:30")));
    }

    @Test
    void acceptsFormattedMarkersAndTimers() {
        assertTrue(DuelGlow.isDuelMatch(List.of(
            "§709/30/26 NA §8D§71§r", "§eTime §fLeft: §700:30")));
        assertTrue(DuelGlow.isDuelMatch(List.of("NA d2", "time left: 00:30")));
    }

    @Test
    void playerNamesDoNotCountAsDuelMarkers() {
        for (String name : List.of("PlayerD1", "D1Player", "Player_D1", "David")) {
            assertFalse(DuelGlow.isDuelMatch(List.of(name, "Time Left: 00:30")), name);
        }
    }
}
