package dev.pheological.hoplite_tweaks.network;

import dev.pheological.hoplite_tweaks.HopliteSession;
import dev.pheological.hoplite_tweaks.HopliteTeammates;
import dev.pheological.hoplite_tweaks.HopliteTweaks;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Let other client entrypoints claim their channels before installing our fallbacks. */
public final class TeammateNetworking {
    private static volatile boolean observeUpdates;
    private static volatile boolean observeResets;

    private TeammateNetworking() {}

    public static void initialize() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            //? >=26 {
            /*var registry = PayloadTypeRegistry.clientboundPlay();
            *///?} else {
            var registry = PayloadTypeRegistry.playS2C();
            //?}
            observeUpdates = !registerIfUnclaimed(registry, TeammatesPayload.TYPE, TeammatesPayload.CODEC, () ->
                ClientPlayNetworking.registerGlobalReceiver(TeammatesPayload.TYPE, (payload, context) -> {
                    if (HopliteSession.isActive()) HopliteTeammates.accept(payload);
                }));
            observeResets = !registerIfUnclaimed(registry, ResetTeammatesPayload.TYPE, ResetTeammatesPayload.CODEC, () ->
                ClientPlayNetworking.registerGlobalReceiver(ResetTeammatesPayload.TYPE,
                    (payload, context) -> HopliteTeammates.reset()));
            if (observeUpdates || observeResets) {
                HopliteTweaks.LOGGER.info("Observing shared teammate channels; preserving the existing mod's codecs and receivers");
            }
        });
    }

    static <T extends CustomPacketPayload> boolean registerIfUnclaimed(
            PayloadTypeRegistry<RegistryFriendlyByteBuf> registry, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Runnable installReceiver) {
        // Fabric's public registry API has no lookup. Never cast another mod's decoded payload to ours.
        if (((PayloadTypeRegistryImpl<RegistryFriendlyByteBuf>) registry).get(type.id()) != null) return false;
        registry.register(type, codec);
        installReceiver.run();
        return true;
    }

    public static CustomPacketPayload copySharedPayload(RegistryFriendlyByteBuf incoming) {
        return copySharedPayload(incoming, observeUpdates, observeResets);
    }

    static CustomPacketPayload copySharedPayload(RegistryFriendlyByteBuf incoming, boolean updates, boolean resets) {
        if (!updates && !resets) return null;
        try {
            RegistryFriendlyByteBuf view = new RegistryFriendlyByteBuf(incoming.duplicate(), incoming.registryAccess());
            var id = view.readIdentifier();
            if (updates && id.equals(TeammatesPayload.TYPE.id())) return TeammatesPayload.CODEC.decode(view);
            if (resets && id.equals(ResetTeammatesPayload.TYPE.id())) return ResetTeammatesPayload.CODEC.decode(view);
        } catch (RuntimeException ignored) {
            // The owning decoder remains responsible for malformed or changed wire formats.
        }
        return null;
    }
}
