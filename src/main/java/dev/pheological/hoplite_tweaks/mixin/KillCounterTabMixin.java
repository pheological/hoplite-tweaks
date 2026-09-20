package dev.pheological.hoplite_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.pheological.hoplite_tweaks.KillCounter;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Run after ordinary tab-name decorators such as LegendWatch. Older LegendWatch
// releases derive their username from the value entering their return modifier;
// seeing our kill suffix first makes their lookup fail and drops every legend icon.
@Mixin(value = PlayerTabOverlay.class, priority = 500)
public abstract class KillCounterTabMixin {
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"), order = 2000)
    private Component hopliteTweaks$kills(Component original, PlayerInfo player) {
        return KillCounter.tab(original, player.getProfile().id());
    }
}
