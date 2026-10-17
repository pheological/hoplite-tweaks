package dev.pheological.hoplite_tweaks.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public interface SlotLockWrappedSlot {
    @Accessor("target") Slot hopliteTweaks$target();
}
