package dev.pheological.hoplite_tweaks.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HopliteTweaksConfigTest {
    @Test
    void preVersionSixConfigEnablesSupplyBeamDistanceWithoutChangingOtherPreferences() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showSupplyBeamDistance = false;
        config.supplyCrateBeams = false;
        config.supplyBeamColor = 0xFF123456;

        HopliteTweaksConfig.migrate(config, 5);

        assertTrue(config.showSupplyBeamDistance);
        assertFalse(config.supplyCrateBeams);
        assertEquals(0xFF123456, config.supplyBeamColor);
    }

    @Test
    void currentConfigPreservesDisabledSupplyBeamDistance() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showSupplyBeamDistance = false;

        HopliteTweaksConfig.migrate(config, 6);

        assertFalse(config.showSupplyBeamDistance);
    }
}
