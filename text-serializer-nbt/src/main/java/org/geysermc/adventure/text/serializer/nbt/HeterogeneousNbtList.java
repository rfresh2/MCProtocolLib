package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

/**
 * Constructs and parses Java Edition's heterogeneous NBT lists.
 *
 * <p>NBT lists must have a single element type. Java Edition stores mixed lists as compound lists,
 * wrapping each non-compound value in a compound with a single empty-string key.</p>
 */
public final class HeterogeneousNbtList {
    private final List<Tag> values = new ArrayList<>();

    /**
     * Constructs a homogeneous NBT list, wrapping mixed values when necessary.
     *
     * @param values the tags to include
     * @return the encoded list
     */
    public static ListTag<?> of(Tag... values) {
        var list = new HeterogeneousNbtList();
        for (var tag : values) {
            list.add(tag);
        }
        return list.build();
    }

    /**
     * Reads and unwraps a list from a compound, returning an empty list if absent.
     *
     * @param map the compound
     * @param key the list key
     * @return the unwrapped tags
     */
    public static List<? extends Tag> getList(CompoundTag map, String key) {
        var list = map.getListTag(key);
        return list == null ? List.of() : tryUnwrap(list);
    }

    /**
     * Unwraps single-empty-key compounds in a compound list.
     *
     * @param list the encoded list
     * @return the unwrapped tags, or the original values for a non-compound list
     */
    public static List<? extends Tag> tryUnwrap(ListTag<?> list) {
        if (list.getElementType() != CompoundTag.class) {
            return list.getValue();
        }
        return list.stream().map(tag -> {
            var compound = (CompoundTag) tag;
            return compound.size() == 1 && compound.contains("") ? compound.get("") : compound;
        }).toList();
    }

    /**
     * Adds a tag to the list.
     *
     * @param value the tag
     */
    public void add(Tag value) {
        values.add(value);
    }

    /**
     * Encodes the accumulated values as a homogeneous NBT list.
     *
     * @return the encoded list
     */
    public ListTag<?> build() {
        if (values.isEmpty() || (!(values.getFirst() instanceof CompoundTag)
            && values.stream().allMatch(tag -> tag.getClass() == values.getFirst().getClass()))) {
            return new ListTag<>(values);
        }
        var compounds = new ListTag<>(CompoundTag.class);
        for (var tag : values) {
            if (tag instanceof CompoundTag compound && !(compound.size() == 1 && compound.contains(""))) {
                compounds.add(compound);
            } else {
                var wrapper = new CompoundTag();
                wrapper.put("", tag);
                compounds.add(wrapper);
            }
        }
        return compounds;
    }

    /**
     * Collects a stream of tags into an encoded NBT list.
     *
     * @param <T> the tag type
     * @return the collector
     */
    public static <T extends Tag> Collector<T, ?, ListTag<?>> collector() {
        return Collector.of(HeterogeneousNbtList::new, HeterogeneousNbtList::add, (first, second) -> {
            first.values.addAll(second.values);
            return first;
        }, HeterogeneousNbtList::build);
    }
}
