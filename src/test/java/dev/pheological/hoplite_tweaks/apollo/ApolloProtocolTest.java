package dev.pheological.hoplite_tweaks.apollo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApolloProtocolTest {
    @org.junit.jupiter.api.BeforeAll
    static void bootstrapMinecraft() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @AfterEach
    void clearState() {
        ApolloState.clear();
    }

    @Test
    void decodesApolloTeamUpdateEnvelope() {
        UUID uuid = UUID.fromString("d7a7c32e-344a-4e78-b9cf-5d1b564d62fd");
        byte[] uuidMessage = message(
            fixed64Field(1, uuid.getMostSignificantBits()),
            fixed64Field(2, uuid.getLeastSignificantBits())
        );
        byte[] location = message(
            bytesField(1, "overworld".getBytes(StandardCharsets.UTF_8)),
            fixed64Field(2, Double.doubleToLongBits(12.5)),
            fixed64Field(3, Double.doubleToLongBits(70.0)),
            fixed64Field(4, Double.doubleToLongBits(-4.25))
        );
        byte[] member = message(
            bytesField(1, uuidMessage),
            bytesField(3, location),
            bytesField(5, "{\"text\":\"A teammate\"}".getBytes(StandardCharsets.UTF_8))
        );
        byte[] update = bytesField(1, member);
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.team.v1.UpdateTeamMembersMessage", update));

        var teammate = ApolloState.teammates().iterator().next();
        assertEquals(uuid, teammate.playerId());
        assertEquals("A teammate", teammate.displayName());
        assertEquals(12.5, teammate.x());
        assertEquals(70, teammate.y());
        assertEquals(-4.25, teammate.z());
        assertEquals("overworld", teammate.world());
    }

    @Test
    void decodesAndRemovesCooldown() {
        byte[] duration = message(varintField(1, 5), varintField(2, 500_000_000));
        byte[] item = bytesField(2, "minecraft:mace".getBytes(StandardCharsets.UTF_8));
        byte[] icon = bytesField(1, item);
        byte[] display = message(
            bytesField(1, "Mace".getBytes(StandardCharsets.UTF_8)),
            bytesField(2, duration),
            bytesField(3, icon)
        );
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.cooldown.v1.DisplayCooldownMessage", display));

        ApolloModels.Cooldown cooldown = ApolloState.cooldowns().iterator().next();
        assertEquals("Mace", cooldown.name());
        assertEquals("minecraft:mace", cooldown.itemId());
        assertTrue(cooldown.durationMillis() >= 5_500);

        byte[] remove = bytesField(1, "Mace".getBytes(StandardCharsets.UTF_8));
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.cooldown.v1.RemoveCooldownMessage", remove));
        assertTrue(ApolloState.cooldowns().isEmpty());
    }

    @Test
    void uuidOnlyNearbyMemberIsRetainedWithoutAnInventedOrigin() {
        UUID id = UUID.randomUUID();
        byte[] member = bytesField(1, message(fixed64Field(1, id.getMostSignificantBits()),
            fixed64Field(2, id.getLeastSignificantBits())));
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.team.v1.UpdateTeamMembersMessage",
            bytesField(1, member)));
        var teammate = ApolloState.teammates().iterator().next();
        assertEquals(id, teammate.playerId());
        org.junit.jupiter.api.Assertions.assertFalse(teammate.hasPosition());
        ApolloState.observeLoadedTeammate(id, new net.minecraft.world.phys.Vec3(4.25, 70, 8),
            "overworld", "NearbyParty");
        teammate = ApolloState.teammates().iterator().next();
        assertEquals(4.25, teammate.x());
        assertEquals("NearbyParty", teammate.displayName());
        // A later UUID-only snapshot preserves the actual observed location, not (0,0,0).
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.team.v1.UpdateTeamMembersMessage",
            bytesField(1, member)));
        assertEquals(4.25, ApolloState.teammates().iterator().next().x());
        assertTrue(ApolloState.teammates().iterator().next().hasPosition());
    }

    @Test
    void teamResetLeavesCooldownsIntactAndUnknownMessagesCannotResetTeams() {
        var id = UUID.randomUUID();
        ApolloState.replaceTeammates(java.util.List.of(new ApolloModels.Teammate(id,
            1, 64, 2, "overworld", "Party", 0xFF4097FF, 1)));
        ApolloState.putCooldown(new ApolloModels.Cooldown("Mace", "minecraft:mace",
            System.currentTimeMillis(), 60_000));
        ApolloProtocol.accept(any("type.googleapis.com/other.ResetTeamMembersMessage", new byte[0]));
        assertEquals(1, ApolloState.teammates().size());
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.team.v1.ResetTeamMembersMessage", new byte[0]));
        assertTrue(ApolloState.teammates().isEmpty());
        assertEquals(1, ApolloState.cooldowns().size());
    }

    @Test
    void partyColorsJsonNamesEmptySnapshotsAndMalformedUpdates() {
        UUID id = UUID.randomUUID();
        byte[] member = message(
            bytesField(1, message(fixed64Field(1, id.getMostSignificantBits()), fixed64Field(2, id.getLeastSignificantBits()))),
            bytesField(4, varintField(1, 0xFF4097FF)),
            bytesField(5, "{\"text\":\"Party \",\"extra\":[{\"text\":\"Player\"}]}".getBytes(StandardCharsets.UTF_8)));
        String type = "type.googleapis.com/lunarclient.apollo.team.v1.UpdateTeamMembersMessage";
        ApolloProtocol.accept(any(type, bytesField(1, member)));
        var saved = ApolloState.teammates().iterator().next();
        assertEquals("Party Player", saved.displayName());
        assertEquals(0xFF4097FF, saved.color());
        ApolloProtocol.accept(any(type, message(bytesField(1, member), new byte[]{10, 127})));
        assertEquals(saved, ApolloState.teammates().iterator().next());
        ApolloProtocol.accept(any(type, new byte[0]));
        assertTrue(ApolloState.teammates().isEmpty());
        assertTrue(ApolloState.lastKnownTeammates().isEmpty());
    }

    @Test
    void serverWorldNamesGateMarkersAndResetOnDisconnect() {
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.player.v1.UpdatePlayerWorldMessage",
            bytesField(1, "hoplite_match_42".getBytes(StandardCharsets.UTF_8))));
        assertTrue(ApolloState.isCurrentWorld("hoplite_match_42", "minecraft:overworld"));
        org.junit.jupiter.api.Assertions.assertFalse(
            ApolloState.isCurrentWorld("hoplite_match_41", "minecraft:overworld"));
        ApolloState.clearTeammates();
        assertEquals("hoplite_match_42", ApolloState.playerWorld());
        ApolloState.clear();
        assertEquals("", ApolloState.playerWorld());
        assertTrue(ApolloState.isCurrentWorld("overworld", "minecraft:overworld"));
    }

    private static byte[] any(String type, byte[] value) {
        return message(
            bytesField(1, type.getBytes(StandardCharsets.UTF_8)),
            bytesField(2, value)
        );
    }

    private static byte[] message(byte[]... fields) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] field : fields) {
            output.writeBytes(field);
        }
        return output.toByteArray();
    }

    private static byte[] bytesField(int number, byte[] value) {
        return message(varint((number << 3) | 2), varint(value.length), value);
    }

    private static byte[] varintField(int number, long value) {
        return message(varint(number << 3), varint(value));
    }

    private static byte[] fixed64Field(int number, long value) {
        return message(
            varint((number << 3) | 1),
            ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array()
        );
    }

    private static byte[] varint(long value) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((value & ~0x7FL) != 0) {
            output.write((int) (value & 0x7F) | 0x80);
            value >>>= 7;
        }
        output.write((int) value);
        return output.toByteArray();
    }
}
