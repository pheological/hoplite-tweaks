package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.HopliteSession;
import dev.pheological.hoplite_tweaks.apollo.ApolloCompatibility;
import dev.pheological.hoplite_tweaks.apollo.ApolloPayloadObserver;
import dev.pheological.hoplite_tweaks.apollo.ApolloProtocol;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Observe before native decoding; never replace a payload or cancel Lunar's handling. */
@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1", priority = 1100)
public abstract class ApolloPayloadObserverMixin {
    @Inject(method = "decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;", at = @At("HEAD"))
    private void hopliteTweaks$observeNativeApollo(FriendlyByteBuf buffer,
            CallbackInfoReturnable<CustomPacketPayload> callback) {
        if (!ApolloCompatibility.observeNativeChannel()) return;
        byte[] data = ApolloPayloadObserver.copyApolloData(buffer);
        if (data == null) return;
        Minecraft.getInstance().execute(() -> {
            if (HopliteSession.isActive()) ApolloProtocol.accept(data);
        });
    }
}
