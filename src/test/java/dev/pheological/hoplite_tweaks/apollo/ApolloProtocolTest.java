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
    @AfterEach
    void clearState() {
        ApolloState.clear();
    }

    @Test
    void decodesTeamUpdateEnvelope() {
        UUID uuid = UUID.fromString("d7a7c32e-344a-4e78-b9cf-5d1b564d62fd");
        byte[] uuidMessage = message(
            varintField(1, uuid.getMostSignificantBits()),
            varintField(2, uuid.getLeastSignificantBits())
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
            bytesField(5, "A teammate".getBytes(StandardCharsets.UTF_8))
        );
        byte[] update = bytesField(1, member);
        ApolloProtocol.accept(any("type.googleapis.com/lunarclient.apollo.team.v1.UpdateTeamMembersMessage", update));

        ApolloModels.Teammate teammate = ApolloState.teammates().iterator().next();
        assertEquals(uuid, teammate.playerId());
        assertEquals(12.5, teammate.x());
        assertEquals("A teammate", teammate.displayName());
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
