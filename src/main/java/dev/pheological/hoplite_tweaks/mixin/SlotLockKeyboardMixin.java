package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.SlotLock;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class SlotLockKeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void hopliteTweaks$ignoreRepeat(long window, int action, KeyEvent event, CallbackInfo ci) {
        SlotLock.keyboardAction(action);
    }
}
