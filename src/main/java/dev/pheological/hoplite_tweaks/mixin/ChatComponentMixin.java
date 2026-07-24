package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.ChatNameHighlighter;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Final display hook for every signed, system, and client-side chat message.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component hopliteTweaks$highlightConfiguredNames(Component message) {
        return ChatNameHighlighter.highlightForDisplay(message);
    }
}
