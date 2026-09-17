package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.ChatNameHighlighter;
import dev.pheological.hoplite_tweaks.KillCounterRenderState;
import dev.pheological.hoplite_tweaks.PlayerNametag;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Styles vanilla's base player name before other nametag mods augment it.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @WrapOperation(
        method = "getNameTag",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component hopliteTweaks$styleConfiguredPlayerNameTag(Entity entity, Operation<Component> original) {
        if (entity instanceof Player player) {
            return ChatNameHighlighter.highlightNameTag(
                player.getGameProfile().name(),
                original.call(entity)
            );
        }
        return original.call(entity);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void hopliteTweaks$capturePlayer(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        KillCounterRenderState extraState = (KillCounterRenderState) state;
        if (!(entity instanceof Player) || state.nameTag == null) {
            extraState.hopliteTweaks$playerId(null);
            extraState.hopliteTweaks$nametagHeader(null);
            return;
        }
        extraState.hopliteTweaks$playerId(entity.getUUID());
        PlayerNametag.Decoration decoration = PlayerNametag.decorate(state.nameTag, entity.getUUID());
        state.nameTag = decoration.name();
        extraState.hopliteTweaks$nametagHeader(decoration.header());
    }
}
