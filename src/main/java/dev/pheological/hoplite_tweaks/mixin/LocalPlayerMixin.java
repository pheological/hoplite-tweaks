package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.DropProtection;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Protects selected hotbar swords and legendaries from accidental single-press drops.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    private static final long DOUBLE_TAP_WINDOW_MILLIS = 600;
    private static long pendingDropUntil;
    private static int pendingSlot = -1;
    private static Item pendingItem;

    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$requireDoubleTapForSwordDrop(
        boolean dropEntireStack,
        CallbackInfoReturnable<Boolean> callback
    ) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = (LocalPlayer) (Object) this;
        ItemStack selected = player.getInventory().getSelectedItem();

        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        boolean protectedSword = config.doubleTapSwordDrop && isSword(selected);
        boolean protectedLegendary = config.doubleTapLegendaryDrop
            && DropProtection.isLegendary(selected);
        if (!config.enabled
            || isScreenOpen(client)
            || (!protectedSword && !protectedLegendary)) {
            clearPendingDrop();
            return;
        }

        long now = System.currentTimeMillis();
        int slot = player.getInventory().getSelectedSlot();
        Item item = selected.getItem();
        if (now <= pendingDropUntil && slot == pendingSlot && item == pendingItem) {
            clearPendingDrop();
            return;
        }

        pendingDropUntil = now + DOUBLE_TAP_WINDOW_MILLIS;
        pendingSlot = slot;
        pendingItem = item;
        DropProtection.showPressAgainMessage();
        callback.setReturnValue(false);
    }

    private static boolean isSword(ItemStack stack) {
        //? >=26 {
        /*return stack.typeHolder().is(ItemTags.SWORDS);
        *///?} else {
        return stack.is(ItemTags.SWORDS);
        //?}
    }

    private static boolean isScreenOpen(Minecraft client) {
        //? >=26.2 {
        /*return client.gui.screen() != null;
        *///?} else {
        return client.screen != null;
        //?}
    }

    private static void clearPendingDrop() {
        pendingDropUntil = 0;
        pendingSlot = -1;
        pendingItem = null;
    }
}
