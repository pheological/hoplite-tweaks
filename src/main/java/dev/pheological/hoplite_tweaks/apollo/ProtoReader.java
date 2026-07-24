package dev.pheological.hoplite_tweaks.apollo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Small, dependency-free protobuf wire reader for the Apollo messages used by Hoplite Tweaks.
 */
final class ProtoReader {
    record Field(int number, int wireType, long numberValue, byte[] bytesValue) {
        String stringValue() {
            return new String(bytesValue, StandardCharsets.UTF_8);
        }

        double doubleValue() {
            return Double.longBitsToDouble(numberValue);
        }
    }

    private final byte[] data;
    private int cursor;

    ProtoReader(byte[] data) {
        this.data = data;
    }

    List<Field> fields() {
        if (data.length == 0) {
            return Collections.emptyList();
        }
        List<Field> fields = new ArrayList<>();
        while (cursor < data.length) {
            long tag = readVarint();
            int number = (int) (tag >>> 3);
            int wireType = (int) (tag & 7);
            if (number == 0) {
                throw new IllegalArgumentException("Invalid protobuf field number");
            }
            fields.add(switch (wireType) {
                case 0 -> new Field(number, wireType, readVarint(), null);
                case 1 -> new Field(number, wireType, readFixed64(), null);
                case 2 -> new Field(number, wireType, 0, readBytes());
                case 5 -> new Field(number, wireType, readFixed32(), null);
                default -> throw new IllegalArgumentException("Unsupported protobuf wire type " + wireType);
            });
        }
        return fields;
    }

    private long readVarint() {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            require(1);
            int value = data[cursor++] & 0xFF;
            result |= (long) (value & 0x7F) << shift;
            if ((value & 0x80) == 0) {
                return result;
            }
        }
        throw new IllegalArgumentException("Malformed protobuf varint");
    }

    private byte[] readBytes() {
        long rawLength = readVarint();
        if (rawLength < 0 || rawLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid protobuf byte length");
        }
        int length = (int) rawLength;
        require(length);
        byte[] result = new byte[length];
        System.arraycopy(data, cursor, result, 0, length);
        cursor += length;
        return result;
    }

    private long readFixed64() {
        require(Long.BYTES);
        long value = ByteBuffer.wrap(data, cursor, Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).getLong();
        cursor += Long.BYTES;
        return value;
    }

    private long readFixed32() {
        require(Integer.BYTES);
        int value = ByteBuffer.wrap(data, cursor, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
        cursor += Integer.BYTES;
        return Integer.toUnsignedLong(value);
    }

    private void require(int count) {
        if (count < 0 || cursor + count > data.length) {
            throw new IllegalArgumentException("Truncated protobuf message");
        }
    }
}
