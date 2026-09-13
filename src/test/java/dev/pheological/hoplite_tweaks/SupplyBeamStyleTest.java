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
}
