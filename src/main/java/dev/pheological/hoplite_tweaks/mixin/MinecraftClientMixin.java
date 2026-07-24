package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.DuelGlow;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void hopliteTweaks$glowDuelTeammates(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if (DuelGlow.shouldGlow(entity)) {
            callback.setReturnValue(true);
        }
    }
}
