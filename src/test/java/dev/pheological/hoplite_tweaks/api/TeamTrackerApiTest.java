package dev.pheological.hoplite_tweaks.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeamTrackerApiTest {
    @Test void compassBearingsMatchEvergreenConvention() {
        assertEquals(0, TeamTrackerApi.bearing(0, -10));
        assertEquals(90, TeamTrackerApi.bearing(10, 0));
        assertEquals(180, TeamTrackerApi.bearing(0, 10));
        assertEquals(270, TeamTrackerApi.bearing(-10, 0));
        assertEquals(45, TeamTrackerApi.bearing(10, -10));
        assertEquals(315, TeamTrackerApi.bearing(-10, -10));
    }

    @Test void dimensionsDoNotMatchBySuffix() {
        assertTrue(TeamTrackerApi.sameDimension("", "minecraft:overworld"));
        assertTrue(TeamTrackerApi.sameDimension("overworld", "minecraft:overworld"));
        assertTrue(TeamTrackerApi.sameDimension("minecraft:overworld", "minecraft:overworld"));
        assertFalse(TeamTrackerApi.sameDimension("custom:overworld", "minecraft:overworld"));
        assertFalse(TeamTrackerApi.sameDimension("minecraft:the_nether", "minecraft:overworld"));
    }
}
