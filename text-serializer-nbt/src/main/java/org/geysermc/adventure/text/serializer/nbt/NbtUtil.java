package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.util.ARGBLike;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

final class NbtUtil {

    static UUID deserializeLenientUUID(Tag tag) {
        if (tag instanceof IntArrayTag array) {
            return deserializeUUID(array.getValue());
        } else if (tag instanceof StringTag string) {
            return UUID.fromString(string.getValue());
        }
        throw new IllegalArgumentException("Don't know how to leniently parse UUID: " + tag);
    }

    static UUID deserializeUUID(int[] array) {
        if (array.length != 4) {
            throw new IllegalArgumentException("UUID int array must have exactly 4 ints, got: " + Arrays.toString(array));
        }
        return new UUID((long) array[0] << 32 | (array[1] & 0xFFFFFFFFL), (long) array[2] << 32 | (array[3] & 0xFFFFFFFFL));
    }

    static int[] serializeUUID(UUID uuid) {
        var mostSignificantBits = uuid.getMostSignificantBits();
        var leastSignificantBits = uuid.getLeastSignificantBits();
        return new int[]{(int) (mostSignificantBits >> 32), (int) mostSignificantBits, (int) (leastSignificantBits >> 32), (int) leastSignificantBits};
    }

    static ARGBLike deserializeARGB(Tag tag) {
        if (tag instanceof ListTag<?> list) {
            if (list.size() != 4 || list.getElementType() != FloatTag.class) {
                throw new IllegalArgumentException("ARGB vector must contain exactly 4 floats, got: " + list);
            }
            return ShadowColor.shadowColor(ratioFloatToByte(((FloatTag) list.get(0)).asFloat()),
                ratioFloatToByte(((FloatTag) list.get(1)).asFloat()), ratioFloatToByte(((FloatTag) list.get(2)).asFloat()),
                ratioFloatToByte(((FloatTag) list.get(3)).asFloat()));
        } else if (tag instanceof NumberTag number) {
            return ShadowColor.shadowColor(number.asInt());
        }
        throw new IllegalArgumentException("Don't know how to parse ARGB colour: " + tag);
    }

    static NumberTag serializeNumber(Number number) {
        return switch (number) {
            case Byte value -> new ByteTag(value);
            case Short value -> new ShortTag(value);
            case Integer value -> new IntTag(value);
            case Long value -> new LongTag(value);
            case Float value -> new FloatTag(value);
            case Double value -> new DoubleTag(value);
            default -> throw new IllegalArgumentException("Unsupported NBT number: " + number);
        };
    }

    private static int ratioFloatToByte(float f) {
        return (int) Math.floor(f * 255.0F);
    }

    static <T> void checkNonNull(@Nullable T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    static void putIfFalse(CompoundTag builder, String key, boolean value) {
        if (!value) {
            builder.putBoolean(key, false);
        }
    }

    static void putIfTrue(CompoundTag builder, String key, boolean value) {
        if (value) {
            builder.putBoolean(key, true);
        }
    }
}
