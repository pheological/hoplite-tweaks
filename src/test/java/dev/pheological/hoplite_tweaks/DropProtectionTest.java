package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropProtectionTest {
    @Test
    void recognizesLegendaryKeywordsCaseInsensitivelyInsideItemNames() {
        assertTrue(DropProtection.isLegendaryName("✦ EMERALD BLADE ✦"));
        assertTrue(DropProtection.isLegendaryName("Corrupted Wither Sickles"));
        assertTrue(DropProtection.isLegendaryName("Poseiden's Relic"));
        assertFalse(DropProtection.isLegendaryName("Diamond Pickaxe"));
    }
}
