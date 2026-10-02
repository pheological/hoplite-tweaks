package dev.pheological.hoplite_tweaks.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
//? >=26.3 {
/*import dev.pheological.hoplite_tweaks.HopliteAutomation;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

@Mixin(ClientPacketListener.class)
public abstract class AutomationMessageMixin {
    //? >=26.3 {
    /*// TAIL runs on the client thread, after the packet's thread check.
    @Inject(method = "handleSystemChat", at = @At("TAIL"))
    private void hopliteTweaks$receiveServerMessage(
        ClientboundSystemChatPacket packet,
        CallbackInfo callback
    ) {
        HopliteAutomation.onGameMessage(packet.content(), packet.overlay());
    }
    *///?}
}
