package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.HopliteSession;
import dev.pheological.hoplite_tweaks.HopliteTeammates;
import dev.pheological.hoplite_tweaks.network.ResetTeammatesPayload;
import dev.pheological.hoplite_tweaks.network.TeammateNetworking;
import dev.pheological.hoplite_tweaks.network.TeammatesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Observe shared channels without consuming bytes or replacing the owner's payload classes. */
@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1", priority = 1100)
public abstract class TeammatePayloadObserverMixin {
    @Inject(method = "decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;", at = @At("HEAD"))
    private void hopliteTweaks$observeSharedTeammates(FriendlyByteBuf buffer,
            CallbackInfoReturnable<CustomPacketPayload> callback) {
        if (!(buffer instanceof RegistryFriendlyByteBuf registryBuffer)) return;
        CustomPacketPayload payload = TeammateNetworking.copySharedPayload(registryBuffer);
        if (payload == null) return;
        Minecraft.getInstance().execute(() -> {
            if (!HopliteSession.isActive()) return;
            if (payload instanceof TeammatesPayload teammates) HopliteTeammates.accept(teammates);
            else if (payload instanceof ResetTeammatesPayload) HopliteTeammates.reset();
        });
    }
}
