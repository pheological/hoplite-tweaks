package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.CooldownTitleHandler;
//? >=26.2 {
/*import net.minecraft.client.gui.Hud;
*///?} else {
import net.minecraft.client.gui.Gui;
//?}
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >=26.2 {
/*@Mixin(Hud.class)
*///?} else {
@Mixin(Gui.class)
//?}
public abstract class GuiMixin {
    @Inject(method = "setTitle", at = @At("HEAD"))
    private void hopliteTweaks$clearCooldownsFromTitle(Component title, CallbackInfo callback) {
        CooldownTitleHandler.onTitle(title);
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void hopliteTweaks$clearCooldownsFromSubtitle(Component subtitle, CallbackInfo callback) {
        CooldownTitleHandler.onTitle(subtitle);
    }
}
