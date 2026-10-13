package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HoverEventSerializerTest {
    static final List<Arguments> HOVER_EVENTS = List.of(
        Arguments.arguments(
            TestTags.compound("action", "show_text", "value", "test 1 2 3"),
            HoverEvent.showText(Component.text("test 1 2 3"))
        ),
        Arguments.arguments(
            TestTags.compound("action", "show_item", "id", "minecraft:stone"),
            HoverEvent.showItem(Key.key("stone"), 1)
        ),
        Arguments.arguments(
            TestTags.compound("action", "show_item", "id", "minecraft:stone"),
            HoverEvent.showItem(Key.key("stone"), 1)
        ),
        Arguments.arguments(
            TestTags.compound("action", "show_item", "id", "minecraft:diamond", "count", 32),
            HoverEvent.showItem(Key.key("diamond"), 32)
        ),
        Arguments.arguments(
            TestTags.compound(
                "action", "show_item",
                "id", "minecraft:diamond_sword",
                "components", TestTags.compound("minecraft:damage", 50, "!minecraft:item_name", new CompoundTag())
            ),
            HoverEvent.showItem(Key.key("diamond_sword"), 1, Map.of(
                Key.key("damage"), new NbtDataComponentValue(TestTags.tag(50)),
                Key.key("item_name"), DataComponentValue.removed()
            ))
        ),
        Arguments.arguments(
            TestTags.compound(
                "action", "show_entity",
                "id", "minecraft:zombie",
                "uuid", new int[]{-1672631430,-75086590,-1822946037,1701451835}
            ),
            HoverEvent.showEntity(Key.key("zombie"), new UUID(-7183897285891832574L, -7829493609586354117L))
        ),
        Arguments.arguments(
            TestTags.compound(
                "action", "show_entity",
                "name", "eclipseisoffline",
                "id", "minecraft:player",
                "uuid", new int[]{-476674174, 1357660845, -1694618559, 456679868}
            ),
            HoverEvent.showEntity(Key.key("player"), new UUID(-2047299986820152659L, -7278331289642966596L), Component.text("eclipseisoffline"))
        )
    );

    @ParameterizedTest
    @FieldSource("HOVER_EVENTS")
    void testDeserialize(CompoundTag map, HoverEvent<?> result) {
        Assertions.assertEquals(result, HoverEventSerializerImpl.deserialize(map, NbtComponentSerializer.nbt()));
    }

    @ParameterizedTest
    @FieldSource("HOVER_EVENTS")
    void testSerialize(CompoundTag result, HoverEvent<?> event) {
        Assertions.assertEquals(result, HoverEventSerializerImpl.serialize(event, NbtComponentSerializer.nbt()));
    }

    @Test
    void testSupportedEvents() {
        HoverEvent.Action.NAMES.values().forEach(action -> Assertions.assertTrue(HoverEventSerializerImpl.SUPPORTED_EVENTS.contains(action),
            "HoverEventSerializerImpl must support hover event: " + action.name()));
    }

    @Test
    void testDeserializeInvalidAction() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HoverEventSerializerImpl.deserialize(TestTags.compound("action", "show_achievement"), NbtComponentSerializer.nbt()));
    }

    @Test
    void testSerializeInvalidAction() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HoverEventSerializerImpl.serialize(HoverEvent.showAchievement("return_to_sender"), NbtComponentSerializer.nbt()));
    }

    @Test
    void testEmptyMapForEmptyComponents() {
        Assertions.assertEquals(new CompoundTag(), HoverEventSerializerImpl.serializeDataComponents(Map.of()));
    }

    @Test
    void testSerializeInvalidDataComponent() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> HoverEventSerializerImpl.serializeDataComponents(Map.of(Key.key("invalid"), new DataComponentValue() {})));
    }
}
