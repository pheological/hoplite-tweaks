package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeammateMarkerRendererTest {
    private static final int KING = 0xFFFFD43B;
    private static final int PARTY = 0xFF4B9CFF;
    private static final int TEAMMATE = 0xFF54E37A;

    @Test
    void kingUsesYellowRoleColor() {
        assertEquals(KING, color("King pheological", 0xFFFFFFFF));
        assertEquals(KING, color("Teammate", 0xFFFFC43D));
    }

    @Test
    void partyUsesBlueRoleColor() {
        assertEquals(PARTY, color("Teammate", 0xFF4097FF));
    }

    @Test
    void regularTeammateUsesGreenRoleColor() {
        assertEquals(TEAMMATE, color("Teammate", 0xFFFFFFFF));
        assertEquals(TEAMMATE, color("Teammate", 0xFF55DD77));
    }

    private int color(String name, int serverColor) {
        ApolloModels.Teammate teammate = new ApolloModels.Teammate(
            UUID.randomUUID(),
            0.0D,
            64.0D,
            0.0D,
            "world",
            name,
            serverColor,
            System.currentTimeMillis()
        );
        return TeammateRole.colorFor(teammate, KING, PARTY, TEAMMATE);
    }
}
