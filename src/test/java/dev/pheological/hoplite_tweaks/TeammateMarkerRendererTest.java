package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import net.minecraft.world.phys.Vec3;
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

    @Test
    void extractsHeartHealthFromFormattedTabName() {
        assertEquals(
            17.5F,
            TeammateMarkerRenderer.extractTabHealth(
                "\u00A7b[TEAM] \u00A7fPHEOLOGICAL \u00A7c17.5❤",
                "PHEOLOGICAL"
            )
        );
    }

    @Test
    void ignoresDigitsInsideUsername() {
        assertEquals(
            18.0F,
            TeammateMarkerRenderer.extractTabHealth("Player123 18", "Player123")
        );
    }

    @Test
    void minimumDistanceHidesTheWholeMarkerInsideItsRadius() {
        assertEquals(true, TeammateMarkerRenderer.outsideMinimumDistance(1.0D, 0));
        assertEquals(false, TeammateMarkerRenderer.outsideMinimumDistance(49.9D, 50));
        assertEquals(false, TeammateMarkerRenderer.outsideMinimumDistance(50.0D, 50));
        assertEquals(true, TeammateMarkerRenderer.outsideMinimumDistance(50.1D, 50));
    }

    @Test
    void markerTextRequiresTargetInsideConfiguredLookCone() {
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
        assertEquals(true, TeammateMarkerRenderer.withinViewAngle(forward,
            directionAtDegrees(15.0D), 15));
        assertEquals(false, TeammateMarkerRenderer.withinViewAngle(forward,
            directionAtDegrees(16.0D), 15));
        assertEquals(true, TeammateMarkerRenderer.withinViewAngle(forward,
            directionAtDegrees(90.0D), 90));
    }

    @Test
    void formatsLastSeenAgeInSecondsThenMinutes() {
        assertEquals("last seen 0s ago", TeammateMarkerRenderer.formatLastSeen(-100));
        assertEquals("last seen 59s ago", TeammateMarkerRenderer.formatLastSeen(59_999));
        assertEquals("last seen 1m ago", TeammateMarkerRenderer.formatLastSeen(60_000));
        assertEquals("last seen 12m ago", TeammateMarkerRenderer.formatLastSeen(729_999));
    }

    @Test
    void deathMarkerFadesFromOpaqueRedToTransparent() {
        assertEquals(0xFFFF3333, TeammateMarkerRenderer.fadedDeathColor(0, 60_000));
        assertEquals(0x80FF3333, TeammateMarkerRenderer.fadedDeathColor(30_000, 60_000));
        assertEquals(0x00FF3333, TeammateMarkerRenderer.fadedDeathColor(60_000, 60_000));
    }

    private Vec3 directionAtDegrees(double degrees) {
        double radians = Math.toRadians(degrees);
        return new Vec3(Math.sin(radians), 0.0D, Math.cos(radians));
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
