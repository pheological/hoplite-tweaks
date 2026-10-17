package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SlotLockCursorTest {
    @Test
    void lockedSlotsBlockThrowButAllowMovementAndTransfers() {
        var state = new SlotLockCursor();
        assertTrue(state.blocks(true, true, true, false, "THROW"));
        for (String action : new String[]{"PICKUP", "QUICK_MOVE", "SWAP", "QUICK_CRAFT"})
            assertFalse(state.blocks(true, true, true, false, action));
        assertFalse(state.blocks(true, true, false, false, "THROW"));
        assertFalse(state.blocks(false, true, true, false, "THROW"));
    }

    @Test
    void cursorFromLockedSlotCannotBeThrownOutsideButCanBePlaced() {
        var state = new SlotLockCursor();
        state.update(false, true, true, true, false);
        for (String action : new String[]{"PICKUP", "THROW"})
            assertTrue(state.blocks(true, false, false, true, action));
        assertFalse(state.blocks(true, false, false, false, "PICKUP"));
        assertFalse(state.blocks(true, false, false, true, "QUICK_CRAFT"));
        assertFalse(state.blocks(false, false, false, true, "PICKUP"));
        state.update(true, true, true, false, false);
        state.update(false, true, true, false, false);
        assertFalse(state.blocks(true, false, false, true, "PICKUP"));
    }

    @Test
    void mergingRetainsProvenanceAndSwappingReplacesIt() {
        var state = new SlotLockCursor();
        state.update(false, true, true, true, false);
        state.update(false, true, true, false, false);
        assertTrue(state.blocks(true, false, false, true, "PICKUP"));
        state.update(false, true, true, false, true);
        assertFalse(state.blocks(true, false, false, true, "PICKUP"));
        state.update(false, false, true, true, false);
        assertFalse(state.blocks(true, false, false, true, "PICKUP"));
    }

    @Test
    void emptyCursorClearsProvenanceEvenWhenClearedByServer() {
        var state = new SlotLockCursor();
        state.update(false, true, true, true, false);
        assertFalse(state.blocks(true, true, false, true, "PICKUP"));
        state.update(false, true, true, false, false);
        assertFalse(state.blocks(true, false, false, true, "PICKUP"));
    }
}
