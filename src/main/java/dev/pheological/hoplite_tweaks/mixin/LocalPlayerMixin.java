package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.SlotLock;
//? >=26.3 {
/*import net.minecraft.client.multiplayer.MultiPlayerGameMode;
*///?}
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//? >=26.3 {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents dropping items from locked hotbar slots.
 */
//? >=26.3 {
/*@Mixin(MultiPlayerGameMode.class)
*///?} else {
@Mixin(LocalPlayer.class)
//?}
public abstract class LocalPlayerMixin {
    //? >=26.3 {
    /*@Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$preventLockedDrop(
        LocalPlayer player,
        boolean dropEntireStack,
        CallbackInfo callback
    ) {
    *///?} else {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$preventLockedDrop(
        boolean dropEntireStack,
        CallbackInfoReturnable<Boolean> callback
    ) {
    //?}
        //? <26.3 {
        LocalPlayer player = (LocalPlayer) (Object) this;
        //?}
        if (!SlotLock.isLocked(player.getInventory().getSelectedSlot())) return;
        //? >=26.3 {
        /*callback.cancel();
        *///?} else {
        callback.setReturnValue(false);
        //?}
    }

}
