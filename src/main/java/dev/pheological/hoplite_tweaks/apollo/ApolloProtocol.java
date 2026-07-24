package dev.pheological.hoplite_tweaks.apollo;

import dev.pheological.hoplite_tweaks.HopliteTweaks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ApolloProtocol {
    private static final String UPDATE_TEAM = "UpdateTeamMembersMessage";
    private static final String RESET_TEAM = "ResetTeamMembersMessage";
    private static final String DISPLAY_COOLDOWN = "DisplayCooldownMessage";
    private static final String REMOVE_COOLDOWN = "RemoveCooldownMessage";
    private static final String RESET_COOLDOWNS = "ResetCooldownsMessage";

    private ApolloProtocol() {
    }

    public static void accept(byte[] packet) {
        try {
            List<ProtoReader.Field> envelope = new ProtoReader(packet).fields();
            String typeUrl = string(envelope, 1, "");
            byte[] value = bytes(envelope, 2);
            if (value == null) {
                return;
            }

            if (typeUrl.endsWith(UPDATE_TEAM)) {
                parseTeamUpdate(value);
            } else if (typeUrl.endsWith(RESET_TEAM)) {
                ApolloState.clearTeammates();
            } else if (typeUrl.endsWith(DISPLAY_COOLDOWN)) {
                parseCooldown(value);
            } else if (typeUrl.endsWith(REMOVE_COOLDOWN)) {
                ApolloState.removeCooldown(string(new ProtoReader(value).fields(), 1, ""));
            } else if (typeUrl.endsWith(RESET_COOLDOWNS)) {
                ApolloState.clearCooldowns();
            }
        } catch (RuntimeException exception) {
            HopliteTweaks.LOGGER.debug("Ignored malformed Apollo payload", exception);
        }
    }

    private static void parseTeamUpdate(byte[] data) {
        long now = System.currentTimeMillis();
        List<ApolloModels.Teammate> teammates = new ArrayList<>();
        for (ProtoReader.Field field : new ProtoReader(data).fields()) {
            if (field.number() != 1 || field.bytesValue() == null) {
                continue;
            }
            List<ProtoReader.Field> member = new ProtoReader(field.bytesValue()).fields();
            List<ProtoReader.Field> uuid = nested(member, 1);
            List<ProtoReader.Field> location = nested(member, 3);
            List<ProtoReader.Field> color = nested(member, 4);
            if (uuid.isEmpty() || location.isEmpty()) {
                continue;
            }
            teammates.add(new ApolloModels.Teammate(
                new UUID(number(uuid, 1, 0), number(uuid, 2, 0)),
                fixedDouble(location, 2),
                fixedDouble(location, 3),
                fixedDouble(location, 4),
                string(location, 1, ""),
                cleanDisplayName(string(member, 5, "")),
                (int) number(color, 1, 0xFF55FFFFL),
                now
            ));
        }
        ApolloState.replaceTeammates(teammates);
    }

    private static void parseCooldown(byte[] data) {
        List<ProtoReader.Field> fields = new ProtoReader(data).fields();
        String name = string(fields, 1, "Cooldown");
        List<ProtoReader.Field> duration = nested(fields, 2);
        long millis = number(duration, 1, 0) * 1_000L + number(duration, 2, 0) / 1_000_000L;
        List<ProtoReader.Field> icon = nested(fields, 3);
        List<ProtoReader.Field> itemStack = nested(icon, 1);
        String itemId = string(itemStack, 2, "minecraft:clock");
        ApolloState.putCooldown(new ApolloModels.Cooldown(name, itemId, System.currentTimeMillis(), millis));
    }

    private static List<ProtoReader.Field> nested(List<ProtoReader.Field> fields, int number) {
        byte[] value = bytes(fields, number);
        return value == null ? List.of() : new ProtoReader(value).fields();
    }

    private static byte[] bytes(List<ProtoReader.Field> fields, int number) {
        for (ProtoReader.Field field : fields) {
            if (field.number() == number && field.bytesValue() != null) {
                return field.bytesValue();
            }
        }
        return null;
    }

    private static String string(List<ProtoReader.Field> fields, int number, String fallback) {
        byte[] value = bytes(fields, number);
        return value == null ? fallback : new String(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static long number(List<ProtoReader.Field> fields, int number, long fallback) {
        for (ProtoReader.Field field : fields) {
            if (field.number() == number && field.bytesValue() == null) {
                return field.numberValue();
            }
        }
        return fallback;
    }

    private static double fixedDouble(List<ProtoReader.Field> fields, int number) {
        for (ProtoReader.Field field : fields) {
            if (field.number() == number && field.wireType() == 1) {
                return field.doubleValue();
            }
        }
        return 0;
    }

    private static String cleanDisplayName(String json) {
        if (json == null || json.isBlank()) {
            return "Teammate";
        }
        String text = json.replaceAll("\\\\u00a7.", "").replaceAll("[{}\\[\\]\"]", " ");
        text = text.replaceAll("\\b(text|color|extra|bold|italic)\\b\\s*:", " ");
        text = text.replaceAll("\\s+", " ").trim();
        return text.isEmpty() ? "Teammate" : text;
    }
}
