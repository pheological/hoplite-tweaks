package dev.pheological.hoplite_tweaks;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuelGlowTest {
    @Test
    void excludesAllTenDuelsLobbies() {
        for (int lobby = 1; lobby <= 10; lobby++) {
            assertFalse(DuelGlow.isDuelMatch("DUELS - NA",
                List.of("09/30/26 00:04 NA D" + lobby, "Division: MASTER", "Total Wins: 4,112")));
            assertFalse(DuelGlow.isDuelMatch("COMPETITIVE", List.of("NA d" + lobby)));
        }
    }

    @Test
    void excludesFormattedLobbyIdentifiers() {
        assertFalse(DuelGlow.isDuelMatch("DUELS - NA", List.of("§709/30/26 NA §8D§71§r")));
    }

    @Test
    void retainsGlowInMatchesAndRequiresDuelsTitle() {
        assertTrue(DuelGlow.isDuelMatch("DUELS - NA", List.of("Round: 1", "Time: 00:30")));
        assertTrue(DuelGlow.isDuelMatch("COMPETITIVE", List.of("Opponent: Player")));
        assertFalse(DuelGlow.isDuelMatch("BATTLE ROYALE", List.of("Round: 1")));
    }

    @Test
    void matchesWholeLobbyIdsRatherThanPlayerNamesOrLargerNumbers() {
        for (String line : List.of("D11", "D100", "D0", "PlayerD1", "D1Player", "Player_D1")) {
            assertTrue(DuelGlow.isDuelMatch("DUELS", List.of(line)), line);
        }
    }
}
