package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.SlotLockClicks;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
//? >=26 {
/*import net.minecraft.world.inventory.ContainerInput;
*///?} else {
import net.minecraft.world.inventory.ClickType;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class)
public abstract class SlotLockCreativeMixin {

    //? >=26 {
    /*@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$protectClick(Slot slot, int slotId, int button, ContainerInput type, CallbackInfo ci) {
    *///?} else {
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$protectClick(Slot slot, int slotId, int button, ClickType type, CallbackInfo ci) {
    //?}
        if (SlotLockClicks.before(((AbstractContainerScreen<?>) (Object) this).getMenu(), slot, slotId, type.name())) ci.cancel();
    }

    @Inject(method = "slotClicked", at = @At("RETURN"))
    private void hopliteTweaks$trackCursor(CallbackInfo ci) {
        SlotLockClicks.after(((AbstractContainerScreen<?>) (Object) this).getMenu());
    }
}
