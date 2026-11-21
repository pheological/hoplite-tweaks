package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.HopliteSession;
import dev.pheological.hoplite_tweaks.apollo.ApolloPayloadObserver;
import dev.pheological.hoplite_tweaks.apollo.ApolloProtocol;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reads Apollo messages before native decoding without registering a channel receiver. */
@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1", priority = 1100)
public abstract class ApolloPayloadObserverMixin {
    @Inject(method = "decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;", at = @At("HEAD"))
    private void hopliteTweaks$observeApollo(FriendlyByteBuf buffer,
            CallbackInfoReturnable<CustomPacketPayload> callback) {
        byte[] data = ApolloPayloadObserver.copyApolloData(buffer);
        if (data == null) return;
        Minecraft.getInstance().execute(() -> {
            if (HopliteSession.isActive()) ApolloProtocol.accept(data);
        });
    }
}
