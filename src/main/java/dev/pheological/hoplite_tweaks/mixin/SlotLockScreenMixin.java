package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.SlotLock;
import dev.pheological.hoplite_tweaks.SlotLockClicks;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
//? >=26 {
/*import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.world.inventory.ClickType;
import net.minecraft.client.gui.GuiGraphics;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class SlotLockScreenMixin {
    @Shadow protected Slot hoveredSlot;

    //? >=26 {
    /*@Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$protectClick(Slot slot, int slotId, int button, ContainerInput type, CallbackInfo ci) {
    *///?} else {
    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$protectClick(Slot slot, int slotId, int button, ClickType type, CallbackInfo ci) {
    //?}
        if (SlotLockClicks.before(((AbstractContainerScreen<?>) (Object) this).getMenu(), slot, slotId, type.name())) ci.cancel();
    }

    //? >=26 {
    /*@Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("RETURN"))
    *///?} else {
    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At("RETURN"))
    //?}
    private void hopliteTweaks$trackCursor(CallbackInfo ci) {
        SlotLockClicks.after(((AbstractContainerScreen<?>) (Object) this).getMenu());
    }


    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$toggleSlot(KeyEvent event, CallbackInfoReturnable<Boolean> ci) {
        Screen screen = (Screen) (Object) this;
        net.minecraft.client.gui.components.events.GuiEventListener focused = screen.getFocused();
        while (focused != null) {
            if (focused instanceof EditBox) return;
            if (focused instanceof net.minecraft.client.gui.components.events.ContainerEventHandler container) focused = container.getFocused();
            else break;
        }
        if (SlotLock.toggle(hoveredSlot, event)) ci.setReturnValue(true);
    }

    //? >=26 {
    /*@Inject(method = "extractSlot", at = @At("RETURN"))
    private void hopliteTweaks$renderLock(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
    *///?} else {
    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void hopliteTweaks$renderLock(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
    //?}
        if (SlotLock.isLocked(SlotLock.playerSlot(slot))) SlotLock.render(graphics, slot.x + 8, slot.y);
    }
}
