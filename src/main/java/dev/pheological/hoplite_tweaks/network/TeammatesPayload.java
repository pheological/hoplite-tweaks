package dev.pheological.hoplite_tweaks.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Hoplite's authoritative Diana/Artemis teammate snapshot. */
public record TeammatesPayload(List<Teammate> teammates) implements CustomPacketPayload {
    public static final Type<TeammatesPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath("hoplite-addons", "update_teammates"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeammatesPayload> CODEC =
        StreamCodec.composite(
            Teammate.CODEC.apply(ByteBufCodecs.list()), TeammatesPayload::teammates,
            TeammatesPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Teammate(
        UUID playerId,
        Component displayName,
        int markerColor,
        ResourceKey<Level> dimension,
        Vec3 position
    ) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Teammate> CODEC =
            StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Teammate::playerId,
                ComponentSerialization.STREAM_CODEC, Teammate::displayName,
                ByteBufCodecs.INT, Teammate::markerColor,
                ResourceKey.streamCodec(Registries.DIMENSION), Teammate::dimension,
                Vec3.STREAM_CODEC, Teammate::position,
                Teammate::new
            );
    }
}
