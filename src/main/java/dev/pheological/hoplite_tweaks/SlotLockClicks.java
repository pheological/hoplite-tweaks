package dev.pheological.hoplite_tweaks;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Shared by survival and creative container screens; never sends inventory packets. */
public final class SlotLockClicks {
    private static final Map<AbstractContainerMenu, Pending> STATES = new WeakHashMap<>();

    private SlotLockClicks() {}

    public static boolean before(AbstractContainerMenu menu, Slot slot, int slotId, String action) {
        Pending state = STATES.computeIfAbsent(menu, unused -> new Pending());
        boolean sourceLocked = SlotLock.isLocked(SlotLock.playerSlot(slot));
        state.cancelled = state.cursor.blocks(SlotLock.active(), menu.getCarried().isEmpty(), sourceLocked, slotId == -999, action);
        if (state.cancelled) return true;
        state.before = menu.getCarried().copy();
        state.pickup = action.equals("PICKUP") || action.equals("PICKUP_ALL");
        state.sourceLocked = sourceLocked && slot != null && slot.hasItem();
        state.swapped = slot != null && slot.hasItem() && !state.before.isEmpty()
            && !ItemStack.isSameItemSameComponents(slot.getItem(), state.before);
        if (action.equals("PICKUP_ALL") && !state.before.isEmpty()) {
            state.sourceLocked = menu.slots.stream().anyMatch(candidate ->
                SlotLock.isLocked(SlotLock.playerSlot(candidate)) && candidate.hasItem()
                    && ItemStack.isSameItemSameComponents(candidate.getItem(), state.before));
        }
        return false;
    }

    public static void after(AbstractContainerMenu menu) {
        Pending state = STATES.get(menu);
        if (state == null || state.cancelled) return;
        ItemStack carried = menu.getCarried();
        boolean changed = carried.getCount() != state.before.getCount()
            || !ItemStack.isSameItemSameComponents(carried, state.before);
        state.cursor.update(carried.isEmpty(), changed, state.pickup, state.sourceLocked, state.swapped);
    }

    private static final class Pending {
        final SlotLockCursor cursor = new SlotLockCursor();
        ItemStack before = ItemStack.EMPTY;
        boolean cancelled;
        boolean pickup;
        boolean sourceLocked;
        boolean swapped;
    }
}
