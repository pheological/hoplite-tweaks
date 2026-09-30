package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.QuakeProKeybind;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "save", at = @At("HEAD"))
    private void hopliteTweaks$saveOriginalPreferences(CallbackInfo ci) {
        QuakeProKeybind.beforeOptionsSave((Options) (Object) this);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void hopliteTweaks$resumeTemporarySettings(CallbackInfo ci) {
        QuakeProKeybind.afterOptionsSave((Options) (Object) this);
    }
}
