package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

public class HeterogeneousNbtListTest {
    static final List<List<Object>> HETEROGENEOUS_LISTS = List.of(
        List.of(5, 4.12F, 4.0, 5000L),
        List.of("1", 5, -6L),
        List.of(TestTags.compound("test", 5), 6.3),
        List.of("21325", TestTags.list(List.of(5.0, 2.1, 3.5)), -4L,
            TestTags.compound("213", 25L))
    );
    static final List<List<Object>> HOMOGENEOUS_LISTS = List.of(
        List.of(1, 2, 5, 4, 3),
        List.of(2L, 1L, 3L, 2L, 6L),
        List.of("hello", "this", "is", "a", "pretty", "gay", "list"),
        List.of(TestTags.compound("1", 2), TestTags.compound("5", 4L)),
        List.of(TestTags.list(List.of(4, 3, 2, 6, 1)), TestTags.list(List.of(1.3F, 5.4F, -2.3F))),
        List.of()
    );

    @ParameterizedTest
    @FieldSource("HETEROGENEOUS_LISTS")
    void testHomogeneousConversion(List<Object> values) {
        var tags = values.stream().map(TestTags::tag).toList();
        // First, use the class to wrap the heterogeneous list into a homogeneous NBT list of maps
        var nbtList = new HeterogeneousNbtList();
        tags.forEach(nbtList::add);
        var wrappedList = nbtList.build();

        Assertions.assertEquals(CompoundTag.class, wrappedList.getElementType(), "type of heterogeneous NBT list must be COMPOUND");

        // Check that each individual value has been wrapped correctly
        for (int i = 0; i < wrappedList.size(); i++) {
            var originalValue = tags.get(i);
            var wrappedValue = (CompoundTag) wrappedList.get(i);

            if (originalValue instanceof CompoundTag) {
                Assertions.assertEquals(originalValue, wrappedValue, "NBT maps in heterogeneous NBT lists must not be wrapped");
            } else {
                Assertions.assertEquals(1, wrappedValue.size(), "wrapped values in heterogeneous NBT lists must have exactly 1 key");
                Assertions.assertEquals(originalValue, wrappedValue.get(""), "wrapped values in heterogeneous NBT lists must be under the '' key");
            }
        }

        // Finally, check unwrapping too, just to be nice
        var unwrapped = HeterogeneousNbtList.tryUnwrap(wrappedList);
        Assertions.assertEquals(tags, unwrapped, "unwrapped heterogeneous NBT list must equal source");
    }

    @ParameterizedTest
    @FieldSource("HOMOGENEOUS_LISTS")
    void testUnwrappedHomogeneousConversion(List<Object> values) {
        var tags = values.stream().map(TestTags::tag).toList();
        // First, use the class to try to "wrap" the heterogeneous list
        // This should essentially be the same as the original list, given the original already was homogeneous
        var nbtList = new HeterogeneousNbtList();
        tags.forEach(nbtList::add);
        var wrappedList = nbtList.build();

        if (values.isEmpty()) {
            Assertions.assertNull(wrappedList.getElementType(), "type of empty homogeneous NBT list must be END");
        } else {
            Assertions.assertEquals(tags.getFirst().getClass(), wrappedList.getElementType(), "type of converted homogeneous NBT list must be same as original");
        }
        Assertions.assertEquals(tags, wrappedList.getValue(), "converted homogeneous NBT list must be same as original");
        if (wrappedList.getElementType() == CompoundTag.class) {
            // tryUnwrap will try to unwrap the compounds and construct a new list
            Assertions.assertEquals(wrappedList.getValue(), HeterogeneousNbtList.tryUnwrap(wrappedList), "unwrapping an already homogeneous NBT list must return the same list");
        } else {
            Assertions.assertSame(wrappedList.getValue(), HeterogeneousNbtList.tryUnwrap(wrappedList), "unwrapping an already homogeneous NBT list must return the same list");
        }
    }

    @Test
    void testShorthandMethods() {
        Assertions.assertTrue(HeterogeneousNbtList.of().isEmpty());
        Assertions.assertEquals(List.of(TestTags.tag(1), TestTags.tag("2"), TestTags.tag(4.5)), HeterogeneousNbtList.tryUnwrap(TestTags.list(1, "2", 4.5)));
    }

    @Test
    void preservesCompoundsThatLookLikeListWrappers() {
        var compound = TestTags.compound("", "value");
        var homogeneous = HeterogeneousNbtList.of(compound);
        Assertions.assertEquals(List.of(compound), HeterogeneousNbtList.tryUnwrap(homogeneous));
        var mixed = HeterogeneousNbtList.of(compound, TestTags.tag(5));
        Assertions.assertEquals(List.of(compound, TestTags.tag(5)), HeterogeneousNbtList.tryUnwrap(mixed));
    }
}
