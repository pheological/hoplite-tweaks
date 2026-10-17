package dev.pheological.hoplite_tweaks.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HopliteTweaksConfigTest {
    @Test
    void slotLocksDefaultEmptyAndIgnoreLegacyDropPreferences() {
        var gson = new com.google.gson.Gson();
        var config = gson.fromJson("{\"doubleTapSwordDrop\":true,\"doubleTapLegendaryDrop\":false}", HopliteTweaksConfig.class);
        assertTrue(config.slotLock);
        assertTrue(config.lockedSlots.isEmpty());
        assertFalse(gson.toJson(config).contains("doubleTap"));
    }

    @Test
    void slotPositionsToggleAndPersistAcrossSerialization() {
        var config = new HopliteTweaksConfig();
        assertTrue(config.toggleSlotLock(0));
        assertTrue(config.toggleSlotLock(35));
        assertFalse(config.toggleSlotLock(-1));
        assertFalse(config.toggleSlotLock(36));
        var gson = new com.google.gson.Gson();
        var restored = gson.fromJson(gson.toJson(config), HopliteTweaksConfig.class);
        assertTrue(restored.isSlotLocked(0));
        assertTrue(restored.isSlotLocked(35));
        assertFalse(restored.isSlotLocked(1));
        assertTrue(restored.toggleSlotLock(0));
        assertFalse(restored.isSlotLocked(0));
    }

    @Test
    void disablingProtectionRetainsLocksAndDoesNotToggleThem() {
        var config = new HopliteTweaksConfig();
        config.toggleSlotLock(8);
        config.slotLock = false;
        assertFalse(config.isSlotLocked(8));
        assertFalse(config.toggleSlotLock(8));
        config.slotLock = true;
        config.enabled = false;
        assertFalse(config.isSlotLocked(8));
        config.enabled = true;
        assertTrue(config.isSlotLocked(8));
        assertFalse(config.isSlotLocked(40));
    }

    @Test
    void autoViewBobbingDefaultsOffAndRoundTrips() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        assertFalse(config.autoViewBobbing);
        com.google.gson.Gson gson = new com.google.gson.Gson();
        assertFalse(gson.fromJson("{}", HopliteTweaksConfig.class).autoViewBobbing);
        config.autoViewBobbing = true;
        assertTrue(gson.fromJson(gson.toJson(config), HopliteTweaksConfig.class).autoViewBobbing);
    }

    @Test
    void teamViewDefaultsMatchOneClientTwentySixPointTwoProfile() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();

        assertTrue(config.teammateMarkers);
        assertTrue(config.showTeammateName);
        assertTrue(config.showTeammateDistance);
        assertTrue(config.alwaysShowTeammateDistance);
        assertTrue(config.revealMarkerTextOnLook);
        assertEquals(5, config.markerTextViewAngle);
        assertTrue(config.hideDistanceWhenTeammateInRenderDistance);
        assertFalse(config.hideMarkerWhenTeammateInRenderDistance);
        assertEquals(100, config.markerScalePercent);
        assertEquals(0, config.markerHeightPercent);
        assertEquals(0, config.markerMinDistance);
        assertEquals(100, config.markerTextScalePercent);
        assertEquals(HopliteTweaksConfig.MarkerShape.CHEVRON, config.markerShape);
        assertTrue(config.markerTextBackground);
    }

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

    @Test
    void preVersionThirteenConfigGetsDisabledGrayTrackerBeamDefaults() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showDisconnectedLocationBeam = true;
        config.showDeathLocationBeam = true;
        config.disconnectedBeamColor = 0xFF123456;
        config.deathBeamColor = 0xFF654321;

        HopliteTweaksConfig.migrate(config, 12);

        assertFalse(config.showDisconnectedLocationBeam);
        assertFalse(config.showDeathLocationBeam);
        assertEquals(0xFF808080, config.disconnectedBeamColor);
        assertEquals(0xFF808080, config.deathBeamColor);
        assertEquals(128, config.trackerBeamHeight);
        assertEquals(100, config.trackerBeamThicknessPercent);
        assertEquals(65, config.trackerBeamOpacityPercent);
    }

    @Test
    void currentConfigPreservesTrackerBeamPreferences() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.showDisconnectedLocationBeam = true;
        config.showDeathLocationBeam = true;
        config.disconnectedBeamColor = 0xFF123456;
        config.deathBeamColor = 0xFF654321;

        HopliteTweaksConfig.migrate(config, 13);

        assertTrue(config.showDisconnectedLocationBeam);
        assertTrue(config.showDeathLocationBeam);
        assertEquals(0xFF123456, config.disconnectedBeamColor);
        assertEquals(0xFF654321, config.deathBeamColor);
    }

    @Test
    void preVersionFourteenAlwaysShowsDistanceByDefault() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.alwaysShowTeammateDistance = false;

        HopliteTweaksConfig.migrate(config, 13);

        assertTrue(config.alwaysShowTeammateDistance);
    }

    @Test
    void currentConfigPreservesAlwaysShowDistancePreference() {
        HopliteTweaksConfig config = new HopliteTweaksConfig();
        config.alwaysShowTeammateDistance = false;

        HopliteTweaksConfig.migrate(config, 14);

        assertFalse(config.alwaysShowTeammateDistance);
    }
}
