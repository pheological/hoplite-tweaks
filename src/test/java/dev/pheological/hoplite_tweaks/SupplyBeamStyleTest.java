package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SupplyBeamStyleTest {
    @Test
    void widthProjectsToTheSameSizeNearFarAndAfterFarPlaneProjection() {
        for (double depth : new double[] {1, 25, 100, 1000, 30000}) {
            double width = SupplyBeamStyle.widthAtDepth(depth, 100);
            assertEquals(0.025D, width / depth, 1e-12);
            double projection = SupplyBeamState.projectionScale(0, 0, depth, 256, width, 64);
            assertEquals(0.025D, (width * projection) / (depth * projection), 1e-12);
        }
    }

    @Test
    void widthRespectsSettingAndNeverInvertsBehindCamera() {
        assertEquals(2 * SupplyBeamStyle.widthAtDepth(100, 100), SupplyBeamStyle.widthAtDepth(100, 200));
        assertEquals(0, SupplyBeamStyle.widthAtDepth(-100, 100));
        assertEquals(0, SupplyBeamStyle.widthAtDepth(0, 100));
    }

    @Test
    void haloFadesToTransparentWhileCoreLightensChosenColor() {
        int gold = 0xA6FFD400;
        assertEquals(0, SupplyBeamStyle.shade(gold, 0, 0) >>> 24);
        assertEquals(gold, SupplyBeamStyle.shade(gold, 1, 0));
        int core = SupplyBeamStyle.shade(gold, 1, 0.8F);
        assertEquals(gold >>> 24, core >>> 24);
        assertTrue((core & 255) > (gold & 255));
        assertTrue((core >>> 8 & 255) > (gold >>> 8 & 255));
    }

    @Test
    void aimTargetFindsAnyHeightAlongBeamAndRejectsMisses() {
        var middle = SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, 1, 0, 64, 100, 64, 100);
        assertNotNull(middle);
        assertEquals(6, middle.heightOffset(), 1e-10);
        assertEquals(100, middle.rayDistance(), 1e-10);

        assertNotNull(SupplyBeamStyle.aimTarget(0, 150, 0, 0, 0, 1, 0, 64, 100, 100, 100));
        assertNotNull(SupplyBeamStyle.aimTarget(0, 150, 0, 0, 0, 1, 0, 64, 100, 80, 100));
        assertNull(SupplyBeamStyle.aimTarget(0, 200, 0, 0, 0, 1, 0, 64, 100, 80, 100));
        assertNull(SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, -1, 0, 64, 100, 64, 100));
        assertNull(SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, 1, 20, 64, 100, 64, 100));
    }

    @Test
    void aimTargetRespectsBeamWidthAndExposesDistanceForNearestSelection() {
        assertNotNull(SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, 1, 17, 64, 100, 64, 100));
        assertNull(SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, 1, 18, 64, 100, 64, 100));

        // An unusually thick beam remains targetable across its entire visible width.
        assertNotNull(SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, 1, 10, 64, 100, 64, 1000));

        var near = SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, 1, 0, 64, 50, 64, 100);
        var far = SupplyBeamStyle.aimTarget(0, 70, 0, 0, 0, 1, 0, 64, 100, 64, 100);
        assertNotNull(near);
        assertNotNull(far);
        assertTrue(near.rayDistance() < far.rayDistance());
    }

    @Test
    void aimTargetHandlesLookingVerticallyAlongBeam() {
        var upward = SupplyBeamStyle.aimTarget(0, 70, 0, 0, 1, 0, 0, 64, 0, 64, 100);
        assertNotNull(upward);
        assertEquals(64, upward.heightOffset(), 1e-10);

        var downward = SupplyBeamStyle.aimTarget(0, 70, 0, 0, -1, 0, 0, 64, 0, 64, 100);
        assertNotNull(downward);
        assertEquals(0, downward.heightOffset(), 1e-10);
    }

    @Test
    void formatsHorizontalDistanceWithOneDecimalPlace() {
        assertEquals("5.0m", SupplyBeamStyle.distanceLabel(0, 0, 3, 4));
        assertEquals("5.0m", SupplyBeamStyle.distanceLabel(-3, -4, 0, 0));
        assertEquals("0.0m", SupplyBeamStyle.distanceLabel(12.5, -9.5, 12.5, -9.5));
    }
}
