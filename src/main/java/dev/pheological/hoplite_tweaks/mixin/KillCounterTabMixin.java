package dev.pheological.hoplite_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.pheological.hoplite_tweaks.KillCounter;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTabOverlay.class)
public abstract class KillCounterTabMixin {
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    private Component hopliteTweaks$kills(Component original, PlayerInfo player) {
        return KillCounter.tab(original, player.getProfile().id());
    }
}
