package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuakeProStateTest {
    private static QuakeProState.Settings settings(int fov, boolean bobbing) {
        return new QuakeProState.Settings(fov, bobbing);
    }

    @Test
    void restoresExactOriginalFovIncludingMaximum() {
        for (int original : new int[] {30, 70, 93, 110}) {
            QuakeProState state = new QuakeProState();
            assertEquals(settings(110, false), state.toggle(settings(original, false), 110, false));
            assertEquals(settings(original, false), state.toggle(settings(100, false), 110, false));
            assertFalse(state.active());
        }
    }

    @Test
    void readsMaximumAndCapturesFreshPreferencesEachTime() {
        QuakeProState state = new QuakeProState();
        assertEquals(settings(120, false), state.toggle(settings(80, false), 120, false));
        state.restore(settings(120, false));
        state.toggle(settings(95, true), 110, true);
        assertEquals(settings(95, true), state.restore(settings(110, false)));
    }

    @Test
    void autoBobbingRestoresBothOriginalValuesAfterManualChanges() {
        for (boolean original : new boolean[] {false, true}) {
            QuakeProState state = new QuakeProState();
            assertEquals(settings(110, true), state.toggle(settings(85, original), 110, true));
            assertEquals(settings(85, original), state.restore(settings(99, !original)));
        }
    }

    @Test
    void disabledAutoBobbingLeavesManualBobbingChangesAlone() {
        QuakeProState state = new QuakeProState();
        state.toggle(settings(75, false), 110, false);
        assertEquals(settings(75, true), state.restore(settings(110, true)));
    }

    @Test
    void savingUsesOriginalPreferencesWithoutEndingTheToggle() {
        QuakeProState state = new QuakeProState();
        state.toggle(settings(90, false), 110, true);
        assertEquals(settings(90, false), state.preferences(settings(110, true)));
        assertTrue(state.active());
        assertEquals(settings(90, false), state.restore(settings(110, true)));
        assertEquals(settings(88, true), state.restore(settings(88, true)));
    }
}
