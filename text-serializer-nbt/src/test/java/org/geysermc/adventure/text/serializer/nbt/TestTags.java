package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;

import java.util.Arrays;
import java.util.List;

/** Helpers for concise tag fixtures. */
final class TestTags {
    static Tag tag(Object value) {
        return switch (value) {
            case Tag tag -> tag;
            case String string -> new StringTag(string);
            case Boolean bool -> new ByteTag((byte) (bool ? 1 : 0));
            case Byte number -> new ByteTag(number);
            case Short number -> new ShortTag(number);
            case Integer number -> new IntTag(number);
            case Long number -> new LongTag(number);
            case Float number -> new FloatTag(number);
            case Double number -> new DoubleTag(number);
            case int[] array -> new IntArrayTag(array);
            default -> throw new IllegalArgumentException("Unsupported test tag: " + value);
        };
    }

    static CompoundTag compound(Object... entries) {
        var compound = new CompoundTag();
        for (var i = 0; i < entries.length; i += 2) {
            compound.put((String) entries[i], tag(entries[i + 1]));
        }
        return compound;
    }

    static ListTag<?> list(Object... values) {
        return Arrays.stream(values).map(TestTags::tag).collect(HeterogeneousNbtList.collector());
    }

    static ListTag<?> list(List<?> values) {
        return list(values.toArray());
    }
}
