package org.geysermc.mcprotocollib.protocol.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Taken from <a href="https://github.com/ViaVersion/ViaVersion/blob/4aefc23bb8074303c713a94d7f583ba4020dda04/common/src/main/java/com/viaversion/viaversion/protocols/protocol1_20_3to1_20_2/util/ComponentConverter.java">ViaVersion's ComponentConverter</a>
 */
public class NbtComponentSerializer {

    private static final Set<String> BOOLEAN_TYPES = new HashSet<>(Arrays.asList(
        "interpret",
        "bold",
        "italic",
        "underlined",
        "strikethrough",
        "obfuscated"
    ));
    // Order is important
    private static final List<Pair<String, String>> COMPONENT_TYPES = Arrays.asList(
        new Pair<>("text", "text"),
        new Pair<>("translatable", "translate"),
        new Pair<>("score", "score"),
        new Pair<>("selector", "selector"),
        new Pair<>("keybind", "keybind"),
        new Pair<>("nbt", "nbt")
    );

    public static @Nullable JsonElement tagComponentToJson(@Nullable final Tag tag) {
        try {
            return convertToJson(null, tag);
        } catch (final Exception e) {
            return new JsonPrimitive("<error>");
        }
    }

    public static @Nullable Tag jsonComponentToTag(@Nullable final JsonElement component) {
        try {
            return convertToTag(component);
        } catch (final Exception e) {
            return new StringTag("<error>");
        }
    }

    private static @Nullable Tag convertToTag(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        } else if (element.isJsonObject()) {
            final CompoundTag tag = new CompoundTag();
            final JsonObject jsonObject = element.getAsJsonObject();
            for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                convertObjectEntry(entry.getKey(), entry.getValue(), tag);
            }

            addComponentType(jsonObject, tag);
            return tag;
        } else if (element.isJsonArray()) {
            return convertJsonArray(element.getAsJsonArray());
        } else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new StringTag(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                return new ByteTag((byte) (primitive.getAsBoolean() ? 1 : 0));
            }

            final Number number = primitive.getAsNumber();
            if (number instanceof Integer) {
                return new IntTag(number.intValue());
            } else if (number instanceof Byte) {
                return new ByteTag(number.byteValue());
            } else if (number instanceof Short) {
                return new ShortTag(number.shortValue());
            } else if (number instanceof Long) {
                return new LongTag(number.longValue());
            } else if (number instanceof Double) {
                return new DoubleTag(number.doubleValue());
            } else if (number instanceof Float) {
                return new FloatTag(number.floatValue());
            } else if (number instanceof LazilyParsedNumber) {
                // TODO: This might need better handling
                return new IntTag(number.intValue());
            }
            return new IntTag(number.intValue()); // ???
        }
        throw new IllegalArgumentException("Unhandled json type " + element.getClass().getSimpleName() + " with value " + element.getAsString());
    }

    private static ListTag convertJsonArray(final JsonArray array) {
        // TODO Number arrays?
        final ListTag listTag = new ListTag();
        boolean singleType = true;
        for (final JsonElement entry : array) {
            final Tag convertedEntryTag = convertToTag(entry);
            if (listTag.getElementType() != null && listTag.getElementType() != convertedEntryTag.getClass()) {
                singleType = false;
                break;
            }

            listTag.add(convertedEntryTag);
        }

        if (singleType) {
            return listTag;
        }

        // Generally, modern vanilla-esque serializers should not produce this format, so it should be rare
        // Lists are only used for lists of components ("extra" and "with")
        final ListTag<CompoundTag> processedListTag = new ListTag(CompoundTag.class);
        for (final JsonElement entry : array) {
            final Tag convertedTag = convertToTag(entry);
            if (convertedTag instanceof CompoundTag compoundTag) {
                processedListTag.add(compoundTag);
                continue;
            }

            // Wrap all entries in compound tags, as lists can only consist of one type of tag
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("type", new StringTag("text"));
            if (convertedTag instanceof ListTag) {
                compoundTag.put("text", new StringTag());
                compoundTag.put("extra", convertedTag);
            } else {
                compoundTag.put("text", new StringTag(convertedTag.asRawString()));
            }
            processedListTag.add(compoundTag);
        }
        return processedListTag;
    }

    /**
     * Converts a json object entry to a tag entry.
     *
     * @param key   key of the entry
     * @param value value of the entry
     * @param tag   the resulting compound tag
     */
    private static void convertObjectEntry(final String key, final JsonElement value, final CompoundTag tag) {
        if ((key.equals("contents")) && value.isJsonObject()) {
            // Store show_entity id as int array instead of uuid string
            // Not really required, but we might as well make it more compact
            final JsonObject hoverEvent = value.getAsJsonObject();
            final CompoundTag convertedTag = (CompoundTag) convertToTag(value);

            final JsonElement id = hoverEvent.get("id");
            final UUID uuid;
            if (id != null && id.isJsonPrimitive() && (uuid = parseUUID(id.getAsString())) != null) {
                convertedTag.remove("id");
                convertedTag.put("id", new IntArrayTag(toIntArray(uuid)));
            }

            tag.put(key, convertedTag);
            return;
        }

        tag.put(key, convertToTag(value));
    }

    private static void addComponentType(final JsonObject object, final CompoundTag tag) {
        if (object.has("type")) {
            return;
        }

        // Add the type to speed up deserialization and make DFU errors slightly more useful
        for (final Pair<String, String> pair : COMPONENT_TYPES) {
            if (object.has(pair.value())) {
                tag.put("type", new StringTag(pair.key()));
                return;
            }
        }
    }

    private static @Nullable JsonElement convertToJson(final @Nullable String key, final @Nullable Tag tag) {
        if (tag == null) {
            return null;
        } else if (tag instanceof CompoundTag compoundTag) {
            final JsonObject object = new JsonObject();
            if (!"value".equals(key)) {
                removeComponentType(object);
            }

            for (final Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                convertCompoundTagEntry(entry.getKey(), entry.getValue(), object);
            }
            return object;
        } else if (tag instanceof ListTag list) {
            final JsonArray array = new JsonArray();
            for (final Object listEntry : list) {
                array.add(convertToJson(null, (Tag) listEntry));
            }
            return array;
        } else if (tag instanceof NumberTag numberTag) {
            if (key != null && BOOLEAN_TYPES.contains(key)) {
                // Booleans don't have a direct representation in nbt
                return new JsonPrimitive(numberTag.asBoolean());
            }
            return new JsonPrimitive(numberTag.getValue());
        } else if (tag instanceof StringTag stringTag) {
            return new JsonPrimitive(stringTag.asRawString());
        } else if (tag instanceof ByteArrayTag arrayTag) {
            final JsonArray array = new JsonArray();
            for (final byte num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof IntArrayTag arrayTag) {
            final JsonArray array = new JsonArray();
            for (final int num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof LongArrayTag arrayTag) {
            final JsonArray array = new JsonArray();
            for (final long num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        }
        throw new IllegalArgumentException("Unhandled tag type " + tag.getClass().getSimpleName());
    }

    private static void convertCompoundTagEntry(final String key, final Tag tag, final JsonObject object) {
        if ((key.equals("contents")) && tag instanceof CompoundTag showEntity) {
            // Back to a UUID string
            final Tag idTag = showEntity.get("id");
            if (idTag instanceof IntArrayTag intArrayTag) {
                showEntity.remove("id");

                final JsonObject convertedElement = (JsonObject) convertToJson(key, tag);
                final UUID uuid = fromIntArray(intArrayTag.getValue());
                convertedElement.addProperty("id", uuid.toString());
                object.add(key, convertedElement);
                return;
            }
        }

        // "":1 is a valid tag, but not a valid json component
        object.add(key.isEmpty() ? "text" : key, convertToJson(key, tag));
    }

    private static void removeComponentType(final JsonObject object) {
        final JsonElement type = object.remove("type");
        if (type == null || !type.isJsonPrimitive()) {
            return;
        }

        // Remove the other fields
        final String typeString = type.getAsString();
        for (final Pair<String, String> pair : COMPONENT_TYPES) {
            if (!pair.key().equals(typeString)) {
                object.remove(pair.value());
            }
        }
    }

    // Last adopted from https://github.com/ViaVersion/ViaVersion/blob/8e38e25cbad1798abb628b4994f4047eaf64640d/common/src/main/java/com/viaversion/viaversion/util/UUIDUtil.java
    public static UUID fromIntArray(final int[] parts) {
        if (parts.length != 4) {
            return new UUID(0, 0);
        }
        return new UUID((long) parts[0] << 32 | (parts[1] & 0xFFFFFFFFL), (long) parts[2] << 32 | (parts[3] & 0xFFFFFFFFL));
    }

    public static int[] toIntArray(final UUID uuid) {
        return toIntArray(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public static int[] toIntArray(final long msb, final long lsb) {
        return new int[]{(int) (msb >> 32), (int) msb, (int) (lsb >> 32), (int) lsb};
    }

    public static @Nullable UUID parseUUID(final String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private record Pair<K, V>(K key, V value) { }
}
