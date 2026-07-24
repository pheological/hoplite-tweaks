package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.ChatNameHighlighter;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Styles configured player names after vanilla and scoreboard-team formatting.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void hopliteTweaks$styleConfiguredPlayerNameTag(
        Entity entity,
        CallbackInfoReturnable<Component> callback
    ) {
        if (entity instanceof Player player) {
            callback.setReturnValue(ChatNameHighlighter.highlightNameTag(
                player.getGameProfile().name(),
                callback.getReturnValue()
            ));
        }
    }
}
