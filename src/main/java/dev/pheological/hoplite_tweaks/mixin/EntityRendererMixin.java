package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.ChatNameHighlighter;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Styles vanilla's base player name before other nametag mods augment it.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Redirect(
        method = "getNameTag",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component hopliteTweaks$styleConfiguredPlayerNameTag(Entity entity) {
        if (entity instanceof Player player) {
            return ChatNameHighlighter.highlightNameTag(
                player.getGameProfile().name(),
                entity.getDisplayName()
            );
        }
        return entity.getDisplayName();
    }
}
