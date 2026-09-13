package dev.pheological.hoplite_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.pheological.hoplite_tweaks.KillCounter;
import net.minecraft.network.chat.MutableComponent;
//? >=26.2 {
/*import net.minecraft.client.gui.Hud;
*///?} else {
import net.minecraft.client.gui.Gui;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? >=26.2 {
/*@Mixin(Hud.class)
*///?} else {
@Mixin(Gui.class)
//?}
public abstract class KillCounterSidebarMixin {
    // The sidebar's row factory runs before both width measurement and drawing.
    // Wildcard covers the differently named synthetic lambda in each version.
    @ModifyExpressionValue(method = "*", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/scores/PlayerTeam;formatNameForTeam(Lnet/minecraft/world/scores/Team;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;"), require = 1)
    private MutableComponent hopliteTweaks$kills(MutableComponent original) {
        return KillCounter.sidebarRow(original);
    }
}
