package dev.pheological.hoplite_tweaks;

/** Tracks provenance only while a stack is held by the inventory cursor. */
public final class SlotLockCursor {
    private boolean locked;

    public boolean blocks(boolean enabled, boolean carriedEmpty, boolean sourceLocked, boolean outside, String action) {
        if (carriedEmpty) locked = false;
        return enabled && ((sourceLocked && action.equals("THROW"))
            || (outside && locked && (action.equals("PICKUP") || action.equals("THROW"))));
    }

    public void update(boolean carriedEmpty, boolean changed, boolean pickedUp, boolean sourceLocked, boolean swapped) {
        if (carriedEmpty) locked = false;
        else if (changed && pickedUp) locked = sourceLocked || (!swapped && locked);
    }
}
