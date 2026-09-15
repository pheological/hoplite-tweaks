package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Extends lava fog to the view distance while retaining its valid color environment. */
@Mixin(LavaFogEnvironment.class)
public abstract class LavaFogEnvironmentMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void hopliteTweaks$removeLavaFog(
        FogData fog,
        Camera camera,
        ClientLevel level,
        float viewDistance,
        DeltaTracker deltaTracker,
        CallbackInfo callback
    ) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (!config.enabled || !config.noLavaFog) {
            return;
        }

        fog.environmentalStart = viewDistance;
        fog.environmentalEnd = viewDistance + 1.0F;
        fog.skyEnd = fog.environmentalEnd;
        fog.cloudEnd = fog.environmentalEnd;
    }
}
