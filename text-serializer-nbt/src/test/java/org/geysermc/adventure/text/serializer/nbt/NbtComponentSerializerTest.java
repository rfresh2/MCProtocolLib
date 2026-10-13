package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NbtComponentSerializerTest {
    private static final CompoundTag TRANSLATE_COMPONENT_TEST_CASE = TestTags.compound("translate", "message.about.cats", "fallback", "another.message");

    static {
        TRANSLATE_COMPONENT_TEST_CASE.put("with", TestTags.list("argument 1", TestTags.compound("text", "argument 2", "color", "gray")));
    }

    static final List<Arguments> GENERAL_COMPONENTS = List.of(
        Arguments.arguments(
            "hiiiiiiiiiiiiii I luv cats",
            Component.text("hiiiiiiiiiiiiii I luv cats")
        ),
        Arguments.arguments(
            TRANSLATE_COMPONENT_TEST_CASE,
            Component.translatable("message.about.cats", "another.message")
                .arguments(Component.text("argument 1"), Component.text("argument 2").color(NamedTextColor.GRAY))
        ),
        Arguments.arguments(
            TestTags.compound("keybind", "key.left", "italic", false),
            Component.keybind("key.left").decoration(TextDecoration.ITALIC, false)
        ),
        Arguments.arguments(
            TestTags.compound("score", TestTags.compound("name", "eclipseisoffline", "objective", "diamonds_mined")),
            Component.score("eclipseisoffline", "diamonds_mined")
        ),
        Arguments.arguments(
            TestTags.compound("selector", "@a"),
            Component.selector("@a")
        ),
        Arguments.arguments(
            TestTags.compound("selector", "@a[tag=cool_people]", "separator", ".-."),
            Component.selector("@a[tag=cool_people]", Component.text(".-."))
        ),
        Arguments.arguments(
            TestTags.compound("nbt", "SelectedItem.id", "entity", "@s"),
            Component.entityNBT("SelectedItem.id", "@s")
        ),
        Arguments.arguments(
            TestTags.compound("sprite", "minecraft:emerald"),
            Component.object(ObjectContents.sprite(Key.key("emerald")))
        )
    );
    static final List<Arguments> TRANSLATION_ARGUMENTS = List.of(
        Arguments.arguments(
            true,
            TranslationArgument.bool(true)
        ),
        Arguments.arguments(
            false,
            TranslationArgument.bool(false)
        ),
        Arguments.arguments(
            5.2,
            TranslationArgument.numeric(5.2)
        ),
        Arguments.arguments(
            -100L,
            TranslationArgument.numeric(-100L)
        ),
        Arguments.arguments(
            (short) 345,
            TranslationArgument.numeric((short) 345)
        ),
        // Note that technically, in vanilla, this is stored as a primitive string, not an actual text component: https://mcsrc.dev/2/26.2/net/minecraft/network/chat/contents/TranslatableContents#L61-63
        // It doesn't matter much since it's basically the same anyway
        Arguments.arguments(
            "hello 1 2 3",
            TranslationArgument.component(Component.text("hello 1 2 3"))
        ),
        Arguments.arguments(
            TestTags.compound("text", "hello", "extra", TestTags.list("2", "4")),
            TranslationArgument.component(Component.text("hello").append(Component.text("2")).append(Component.text("4")))
        ),
        Arguments.arguments(
            TestTags.compound(
                "translate", "string.3",
                "extra", TestTags.list(TestTags.compound("text", "hello", "bold", true)),
                "color", "red"
            ),
            TranslationArgument.component(Component.translatable("string.3")
                .append(Component.text("hello").decorate(TextDecoration.BOLD))
                .color(NamedTextColor.RED))
        )
    );
    static final List<Arguments> NBT_COMPONENTS = List.of(
        Arguments.arguments(
            TestTags.compound("text", "this is definitely not an nbt contents component"),
            Optional.empty()
        ),
        Arguments.arguments(
            TestTags.compound("nbt", "Inventory[0].components.\"minecraft:enchantments\"", "entity", "@s"),
            Optional.of(Component.entityNBT("Inventory[0].components.\"minecraft:enchantments\"", "@s"))
        ),
        Arguments.arguments(
            TestTags.compound("nbt", "my_custom.storage_key", "interpret", true, "storage", "geyser:custom_storage"),
            Optional.of(Component.storageNBT("my_custom.storage_key", Key.key("geyser", "custom_storage")).interpret(true))
        ),
        /*Arguments.arguments(
            TestTags.compound("nbt", "Items[0]", "plain", true, "block", "~ ~5 ~"),
            Optional.of(Component.blockNBT("Items[0]", BlockNBTComponent.Pos.fromString("~ ~5 ~")).plain(true))
        ),
        Arguments.arguments(
            TestTags.compound("nbt", "Items[5]", "block", "^ ^ ^15"),
            Optional.of(Component.blockNBT("Items[5]", BlockNBTComponent.Pos.fromString("^ ^ ^15")))
        ),*/ // FIXME enable when adventure fixes Pos.fromstring
        Arguments.arguments(
            TestTags.compound("nbt", "Items[3]", "separator", ".", "block", "1 2 3"),
            Optional.of(Component.blockNBT("Items[3]", BlockNBTComponent.Pos.fromString("1 2 3")).separator(Component.text(".")))
        )
    );
    static final List<Arguments> OBJECT_COMPONENTS = List.of(
        Arguments.arguments(
            TestTags.compound("text", "dummy test hello"),
            Optional.empty()
        ),
        Arguments.arguments(
            TestTags.compound("sprite", "geyser:gayser_logo"),
            Optional.of(Component.object(ObjectContents.sprite(Key.key("geyser", "gayser_logo"))))
        ),
        Arguments.arguments(
            TestTags.compound(
                "atlas", "minecraft:item",
                "sprite", "geyser:gay_diamond",
                "fallback", "a gay diamond alt text"
            ),
            Optional.of(Component.object(ObjectContents.sprite(Key.key("item"), Key.key("geyser", "gay_diamond"))).fallback(Component.text("a gay diamond alt text")))
        ),
        Arguments.arguments(
            TestTags.compound("player", TestTags.compound("name", "AuriiU")),
            Optional.of(Component.object(ObjectContents.playerHead("AuriiU")))
        ),
        Arguments.arguments(
            TestTags.compound(
                "player", TestTags.compound("id", new int[]{568551383, 634342971, -1301730230, 805981675}),
                "hat", false,
                "fallback", TestTags.compound("keybind", "key.forward", "strikethrough", true)
            ),
            Optional.of(Component.object(ObjectContents.playerHead()
                .id(new UUID(2441909596714913339L, -5590888765258576405L))
                .hat(false)
                .build()).fallback(Component.keybind("key.forward").decorate(TextDecoration.STRIKETHROUGH)))
        )
    );
    static final List<Arguments> LIST_COMPONENTS = List.of(
        Arguments.arguments(
            TestTags.list("test", "5", "4", "6"),
            // When parsing lists, vanilla takes the first component, and appends the rest to that as children
            Component.text("test")
                .append(Component.text("5"))
                .append(Component.text("4"))
                .append(Component.text("6"))
        ),
        Arguments.arguments(
            TestTags.list("test", TestTags.list("5", "4", "6")),
            Component.text("test")
                .append(Component.text("5")
                    .append(Component.text("4"))
                    .append(Component.text("6")))
        ),
        Arguments.arguments(
            TestTags.list(
                TestTags.compound("translate", "hello.world", "color", "#BBEEBB", "bold", false),
                TestTags.list("123", TestTags.compound("text", "done!", "bold", true))
            ),
            Component.translatable("hello.world")
                .color(TextColor.fromHexString("#BBEEBB"))
                .decoration(TextDecoration.BOLD, false)
                .append(Component.text("123")
                    .append(Component.text("done!")
                        .decorate(TextDecoration.BOLD)))
        )
    );

    private final NbtComponentSerializerImpl serializer = (NbtComponentSerializerImpl) NbtComponentSerializer.nbt();

    @ParameterizedTest
    @FieldSource("GENERAL_COMPONENTS")
    void testDeserialize(Object object, Component result) {
        Assertions.assertEquals(result, serializer.deserialize(TestTags.tag(object)));
    }

    @ParameterizedTest
    @FieldSource("GENERAL_COMPONENTS")
    void testSerialize(Object result, Component component) {
        Assertions.assertEquals(TestTags.tag(result), serializer.serialize(component));
    }

    @ParameterizedTest
    @FieldSource("TRANSLATION_ARGUMENTS")
    void testDeserializeTranslationArgument(Object object, TranslationArgument result) {
        Assertions.assertEquals(result, serializer.deserializeTranslationArgument(TestTags.tag(object)));
    }

    @ParameterizedTest
    @FieldSource("TRANSLATION_ARGUMENTS")
    void testSerializeTranslationArgument(Object result, TranslationArgument argument) {
        Assertions.assertEquals(TestTags.tag(result), serializer.serializeTranslationArgument(argument));
    }

    @ParameterizedTest
    @FieldSource("NBT_COMPONENTS")
    void testDeserializeFuzzyNbtContentsComponent(CompoundTag map, Optional<NBTComponent<?>> result) {
        Assertions.assertEquals(result, serializer.deserializeFuzzyNbtContentsComponent(map).map(ComponentBuilder::build));
    }

    @ParameterizedTest
    @FieldSource("NBT_COMPONENTS")
    void testSerializeNbtContentsComponent(CompoundTag result, Optional<NBTComponent<?>> component) {
        // If empty then this component isn't an NBT contents component (for deserializing test only), so skip
        component.ifPresent(nbtComponent -> Assertions.assertEquals(result, serializer.serializeNbtContentsComponent(nbtComponent)));
    }

    @ParameterizedTest
    @FieldSource("OBJECT_COMPONENTS")
    void testDeserializeFuzzyObjectComponent(CompoundTag map, Optional<ObjectComponent> result) {
        Assertions.assertEquals(result, serializer.deserializeFuzzyObjectComponent(map).map(ComponentBuilder::build));
    }

    @ParameterizedTest
    @FieldSource("OBJECT_COMPONENTS")
    void testSerializeObjectComponent(CompoundTag result, Optional<ObjectComponent> component) {
        // If empty then this component isn't an object component (for deserializing test only), so skip
        component.ifPresent(objectComponent -> Assertions.assertEquals(result, serializer.serializeObjectComponent(objectComponent)));
    }

    @ParameterizedTest
    @FieldSource("LIST_COMPONENTS")
    void testListComponentDeserialize(ListTag<?> list, Component result) {
        Assertions.assertEquals(result, serializer.deserialize(list));
    }

    @Test
    void testDeserializeInvalidComponent() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(TestTags.tag(5)), "deserialize must throw IllegalArgumentException for invalid components");
    }

    @Test
    void testDeserializeInvalidNbtContentsComponent() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> serializer.deserializeFuzzyNbtContentsComponent(TestTags.compound("nbt", "path_without_spruce")),
            "deserializeFuzzyNbtContentsComponent must throw IllegalArgumentException for components without recognised source");
    }
}
