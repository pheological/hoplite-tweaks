package dev.pheological.hoplite_tweaks.apollo;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.pheological.hoplite_tweaks.HopliteTeammates;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Independent decoder for the public lunarclient.apollo.team.v1 protobuf schema.
 * Schema: https://buf.build/lunarclient/apollo (official apollo-protos 0.2.7).
 * No implementation code or assets are taken from other client mods.
 */
final class ApolloTeamProtocol {
    private static final String TEAM = "lunarclient.apollo.team.v1.";

    private ApolloTeamProtocol() {}

    static boolean accept(String typeUrl, byte[] value) {
        String type = typeUrl.substring(typeUrl.lastIndexOf('/') + 1);
        if (type.equals(TEAM + "ResetTeamMembersMessage")) {
            new ProtoReader(value).fields();
            ApolloState.clearTeammates();
            return true;
        }
        if (!type.equals(TEAM + "UpdateTeamMembersMessage")) return false;
        var members = new ArrayList<ApolloModels.Teammate>();
        for (var field : new ProtoReader(value).fields()) {
            if (field.number() != 1 || field.wireType() != 2) continue;
            var member = decodeMember(field.bytesValue());
            if (member != null) members.add(member);
        }
        // Parse the complete snapshot before replacing state: malformed updates are atomic.
        HopliteTeammates.accept(members);
        return true;
    }

    private static ApolloModels.Teammate decodeMember(byte[] data) {
        var fields = new ProtoReader(data).fields();
        byte[] idBytes = bytes(fields, 1);
        if (idBytes == null) return null;
        var idFields = new ProtoReader(idBytes).fields();
        UUID id = new UUID(number(idFields, 1), number(idFields, 2));
        if (id.equals(new UUID(0, 0))) return null;
        byte[] locationBytes = bytes(fields, 3);
        var location = locationBytes == null ? List.<ProtoReader.Field>of()
            : new ProtoReader(locationBytes).fields();
        double x = locationBytes == null ? Double.NaN : coordinate(location, 2);
        double y = locationBytes == null ? Double.NaN : coordinate(location, 3);
        double z = locationBytes == null ? Double.NaN : coordinate(location, 4);
        if (locationBytes != null && (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))) {
            throw new IllegalArgumentException("Non-finite Apollo team location");
        }
        byte[] colorBytes = bytes(fields, 4);
        int color = colorBytes == null ? 0xFFFFFFFF : (int) number(new ProtoReader(colorBytes).fields(), 1);
        String name = string(fields, 5);
        if (!name.isBlank()) {
            name = jsonText(JsonParser.parseString(name), 0);
        } else {
            byte[] legacyName = bytes(fields, 2);
            name = legacyName == null ? "" : legacyText(legacyName, 0);
        }
        return new ApolloModels.Teammate(id, x, y, z, string(location, 1), name, color,
            System.currentTimeMillis());
    }

    private static String legacyText(byte[] data, int depth) {
        if (depth > 32) throw new IllegalArgumentException("Nested Apollo component");
        var fields = new ProtoReader(data).fields();
        var result = new StringBuilder(string(fields, 1));
        for (var field : fields) {
            if (field.number() == 4 && field.wireType() == 2) {
                result.append(legacyText(field.bytesValue(), depth + 1));
            }
        }
        return result.toString();
    }

    private static String jsonText(JsonElement element, int depth) {
        if (depth > 32) throw new IllegalArgumentException("Nested Apollo name");
        if (element.isJsonNull()) return "";
        if (element.isJsonPrimitive()) return element.getAsString();
        var result = new StringBuilder();
        if (element.isJsonArray()) {
            for (var child : element.getAsJsonArray()) result.append(jsonText(child, depth + 1));
        } else {
            var object = element.getAsJsonObject();
            if (object.has("text")) result.append(object.get("text").getAsString());
            if (object.has("extra")) result.append(jsonText(object.get("extra"), depth + 1));
        }
        return result.toString();
    }

    private static byte[] bytes(List<ProtoReader.Field> fields, int number) {
        return fields.stream().filter(f -> f.number() == number && f.wireType() == 2)
            .map(ProtoReader.Field::bytesValue).findFirst().orElse(null);
    }

    private static String string(List<ProtoReader.Field> fields, int number) {
        byte[] value = bytes(fields, number);
        return value == null ? "" : new String(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static long number(List<ProtoReader.Field> fields, int number) {
        return fields.stream().filter(f -> f.number() == number && f.wireType() != 2)
            .mapToLong(ProtoReader.Field::numberValue).findFirst().orElse(0L);
    }

    private static double coordinate(List<ProtoReader.Field> fields, int number) {
        return fields.stream().filter(f -> f.number() == number && f.wireType() == 1)
            .mapToDouble(ProtoReader.Field::doubleValue).findFirst().orElse(0D);
    }
}
