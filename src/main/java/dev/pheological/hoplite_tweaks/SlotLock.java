package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
//? >=26 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Locks inventory positions, independently of the items occupying them. */
public final class SlotLock {
    private static KeyMapping toggleKey;
    private static boolean initialKeyPress;
    private static final Identifier ICON = Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "textures/gui/lock.png");

    private SlotLock() {}

    public static void initialize() {
        //? >=26 {
        /*toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?} else {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?}
            //? >=26.3 {
            /*"key.hoplite_tweaks.slot_lock", InputConstants.Type.KEYBOARD,
            *///?} else {
            "key.hoplite_tweaks.slot_lock", InputConstants.Type.KEYSYM,
            //?}
            InputConstants.KEY_L, HopliteKeybindings.CATEGORY));
    }

    public static boolean active() {
        var config = HopliteTweaksConfig.get();
        return config.enabled && config.slotLock;
    }

    public static boolean isLocked(int slot) {
        return HopliteTweaksConfig.get().isSlotLocked(slot);
    }

    public static int playerSlot(Slot slot) {
        if (slot instanceof dev.pheological.hoplite_tweaks.mixin.SlotLockWrappedSlot wrapped) slot = wrapped.hopliteTweaks$target();
        var player = Minecraft.getInstance().player;
        if (slot == null || player == null || slot.container != player.getInventory()) return -1;
        int index = slot.getContainerSlot();
        return index >= 0 && index < 36 ? index : -1;
    }

    public static void keyboardAction(int action) {
        initialKeyPress = action == 1;
    }

    public static boolean toggle(Slot slot, KeyEvent event) {
        int index = playerSlot(slot);
        if (!active() || index < 0 || !toggleKey.matches(event)) return false;
        if (!initialKeyPress) return true;
        var config = HopliteTweaksConfig.get();
        config.toggleSlotLock(index);
        HopliteTweaksConfig.save();
        return true;
    }

    //? >=26 {
    /*public static void render(GuiGraphicsExtractor graphics, int x, int y) {
    *///?} else {
    public static void render(GuiGraphics graphics, int x, int y) {
    //?}
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, x, y, 0, 0, 8, 8, 8, 8);
    }
}
