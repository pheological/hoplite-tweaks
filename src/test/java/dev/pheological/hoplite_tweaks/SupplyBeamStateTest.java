package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplyBeamStateTest {
    private static String announcement(String x, String z) {
        return "⚠ A supply drop is spawning near you at X=" + x + " and Z=" + z
            + " Use your Supply Drop Tracker to navigate towards its location.";
    }

    @Test
    void parsesScreenshotAndSignedFormattedCoordinates() {
        assertEquals(new SupplyBeamState.Location(0, 0), SupplyBeamState.parse(announcement("0", "0")));
        assertEquals(new SupplyBeamState.Location(0, 0), SupplyBeamState.parse(
            announcement("0", "0").replace("⚠", "❗")));
        assertEquals(new SupplyBeamState.Location(-120, 45), SupplyBeamState.parse(
            announcement("§a-120§r", "+45").replace(" and ", "\n and ")));
    }

    @Test
    void rejectsPlayerChatMalformedAndOutOfRangeCoordinates() {
        assertNull(SupplyBeamState.parse("Player: " + announcement("1", "2")));
        assertNull(SupplyBeamState.parse(announcement("oops", "2")));
        assertNull(SupplyBeamState.parse(announcement("99999999999999999", "2")));
        assertNull(SupplyBeamState.parse(announcement("30000001", "2")));
        assertNull(SupplyBeamState.parse("Meet me at X=100 and Z=100"));
        assertNull(SupplyBeamState.parse(null));
    }

    @Test
    void tracksMultipleDropsAndDuplicateDoesNotExtendLifetime() {
        var state = state();
        var a = new SupplyBeamState.Location(100, 100);
        var b = new SupplyBeamState.Location(-200, -200);
        state.announce(a, 0);
        state.announce(b, 1000);
        state.announce(a, 2000);
        assertEquals(2, state.drops().size());
        state.tick(SupplyBeamState.LIFETIME_MS - 1, 0, 0, 25);
        assertEquals(2, state.drops().size());
        state.tick(SupplyBeamState.LIFETIME_MS, 0, 0, 25);
        assertEquals(b, state.drops().getFirst().position());
        state.tick(SupplyBeamState.LIFETIME_MS + 1000, 0, 0, 25);
        assertTrue(state.drops().isEmpty());
    }

    @Test
    void arrivalUsesHorizontalRadiusAndDoesNotResurrectOnDuplicate() {
        var state = state();
        var location = new SupplyBeamState.Location(100, 100);
        state.announce(location, 0);
        state.tick(1, 74.99, 100, 25);
        assertEquals(1, state.drops().size());
        state.tick(2, 75, 100, 25);
        assertTrue(state.drops().isEmpty());
        state.announce(location, 3);
        state.tick(4, 0, 0, 25);
        assertTrue(state.drops().isEmpty());
        state.announce(location, SupplyBeamState.LIFETIME_MS);
        assertEquals(1, state.drops().size());
    }

    @Test
    void zeroRadiusKeepsDropEvenAtDestinationAndTrackingNeedsNoVisibilityFlag() {
        var state = state();
        state.announce(new SupplyBeamState.Location(100, 100), 0);
        state.tick(1, 100, 100, 0);
        assertEquals(1, state.drops().size());
        state.tick(SupplyBeamState.LIFETIME_MS, 100, 100, 0);
        assertTrue(state.drops().isEmpty());
    }

    @Test
    void worldChangeClearsDropsAndDeduplication() {
        var state = state();
        var location = new SupplyBeamState.Location(100, 100);
        state.announce(location, 0);
        state.enterWorld(new Object());
        assertTrue(state.drops().isEmpty());
        state.announce(location, 2);
        assertEquals(1, state.drops().size());
        state.reset();
        assertTrue(state.drops().isEmpty());
    }

    @Test
    void learnedGroundHeightSurvivesChunkUnloadingWithoutChangingExpiry() {
        var state = state();
        var location = new SupplyBeamState.Location(100, 100);
        state.announce(location, 0);
        assertEquals(64, state.drops().getFirst().groundY());
        state.groundHeight(location, null);
        assertEquals(64, state.drops().getFirst().groundY());
        state.groundHeight(location, 120);
        assertEquals(120, state.drops().getFirst().groundY());
        state.groundHeight(location, null);
        assertEquals(120, state.drops().getFirst().groundY());
        state.groundHeight(location, null);
        assertEquals(120, state.drops().getFirst().groundY());
        state.groundHeight(location, 72);
        assertEquals(72, state.drops().getFirst().groundY());
        state.groundHeight(location, null);
        assertEquals(72, state.drops().getFirst().groundY());
        assertEquals(0, state.drops().getFirst().announcedAt());
        state.tick(SupplyBeamState.LIFETIME_MS, 0, 0, 25);
        assertTrue(state.drops().isEmpty());
    }

    @Test
    void manualClearKeepsDuplicateSuppression() {
        var state = state();
        var location = new SupplyBeamState.Location(100, 100);
        state.announce(location, 0);
        state.clear();
        state.announce(location, 1);
        assertTrue(state.drops().isEmpty());
    }

    @Test
    void projectionPreservesDirectionAndContainsWholeColumn() {
        assertEquals(1, SupplyBeamState.projectionScale(1, 0, 1, 32, 1, 64));
        double x = 3000, y = -40, z = -2000, height = 512, width = 5, limit = 64;
        double scale = SupplyBeamState.projectionScale(x, y, z, height, width, limit);
        assertTrue(scale > 0 && scale < 1);
        assertEquals(x / z, (x * scale) / (z * scale), 1e-10);
        assertEquals((y + height) / z, ((y + height) * scale) / (z * scale), 1e-10);
        assertTrue((Math.sqrt(x * x + (y + height) * (y + height) + z * z) + width) * scale <= limit + 1e-10);
    }

    private static SupplyBeamState state() {
        var state = new SupplyBeamState();
        state.enterWorld(new Object());
        return state;
    }
}
