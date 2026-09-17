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

    @Test
    void preVersionSevenConfigEnablesNoLavaFog() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.noLavaFog = false;

        HopliteTweaksConfig.migrate(config, 6);

        assertTrue(config.noLavaFog);
    }

    @Test
    void currentConfigPreservesDisabledNoLavaFog() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.noLavaFog = false;

        HopliteTweaksConfig.migrate(config, 7);

        assertFalse(config.noLavaFog);
    }

    @Test
    void preVersionEightConfigGetsConservativeLookRevealDefaults() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.revealMarkerTextOnLook = true;
        config.markerTextViewAngle = 80;

        HopliteTweaksConfig.migrate(config, 7);

        assertFalse(config.revealMarkerTextOnLook);
        assertEquals(15, config.markerTextViewAngle);
    }

    @Test
    void preVersionNineConfigEnablesGrayLastKnownMarkers() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showLastKnownLocations = false;
        config.lastKnownMarkerColor = 0xFF123456;

        HopliteTweaksConfig.migrate(config, 8);

        assertTrue(config.showLastKnownLocations);
        assertEquals(0xFF808080, config.lastKnownMarkerColor);
    }

    @Test
    void currentConfigPreservesLastKnownPreferences() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showLastKnownLocations = false;
        config.lastKnownMarkerColor = 0xFF123456;

        HopliteTweaksConfig.migrate(config, 9);

        assertFalse(config.showLastKnownLocations);
        assertEquals(0xFF123456, config.lastKnownMarkerColor);
    }

    @Test
    void preVersionTenConfigEnablesDefaultDripstoneBadgePlacement() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.dripstoneBadge = false;
        config.dripstoneDisplay = HopliteTweaksConfig.KillDisplay.NAMETAG;
        config.dripstonePlacement = HopliteTweaksConfig.KillPlacement.ABOVE_NAME;

        HopliteTweaksConfig.migrate(config, 9);

        assertTrue(config.dripstoneBadge);
        assertEquals(HopliteTweaksConfig.KillDisplay.BOTH, config.dripstoneDisplay);
        assertEquals(HopliteTweaksConfig.KillPlacement.NEXT_TO_NAME, config.dripstonePlacement);
    }

    @Test
    void currentConfigPreservesDripstoneBadgePreferences() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.dripstoneBadge = false;
        config.dripstoneDisplay = HopliteTweaksConfig.KillDisplay.TAB_LIST;
        config.dripstonePlacement = HopliteTweaksConfig.KillPlacement.ABOVE_NAME;

        HopliteTweaksConfig.migrate(config, 10);

        assertFalse(config.dripstoneBadge);
        assertEquals(HopliteTweaksConfig.KillDisplay.TAB_LIST, config.dripstoneDisplay);
        assertEquals(HopliteTweaksConfig.KillPlacement.ABOVE_NAME, config.dripstonePlacement);
    }

    @Test
    void preVersionElevenConfigEnablesAutoDamageSummary() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.autoDamageSummary = false;

        HopliteTweaksConfig.migrate(config, 10);

        assertTrue(config.autoDamageSummary);
    }

    @Test
    void currentConfigPreservesDisabledAutoDamageSummary() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.autoDamageSummary = false;

        HopliteTweaksConfig.migrate(config, 11);

        assertFalse(config.autoDamageSummary);
    }

    @Test
    void preVersionTwelveConfigGetsDeathMarkerDefaults() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showDeathLocations = false;
        config.deathMarkerDurationSeconds = 15;

        HopliteTweaksConfig.migrate(config, 11);

        assertTrue(config.showDeathLocations);
        assertEquals(60, config.deathMarkerDurationSeconds);
    }

    @Test
    void currentConfigPreservesDeathMarkerPreferences() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showDeathLocations = false;
        config.deathMarkerDurationSeconds = 90;

        HopliteTweaksConfig.migrate(config, 12);

        assertFalse(config.showDeathLocations);
        assertEquals(90, config.deathMarkerDurationSeconds);
    }
}
