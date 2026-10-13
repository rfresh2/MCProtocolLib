package org.geysermc.mcprotocollib.protocol.codec;

import com.viaversion.nbt.io.MNBTIO;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import org.geysermc.adventure.text.serializer.nbt.HeterogeneousNbtList;
import org.geysermc.adventure.text.serializer.nbt.NbtDataComponentValue;
import org.geysermc.adventure.text.serializer.nbt.NbtDialog;
import org.geysermc.mcprotocollib.protocol.data.DefaultComponentSerializer;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.ItemTypes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenNbtCodecTest {
    @Test
    void readsStringCompoundAndMixedListComponentsWithoutConsumingFollowingData() {
        var styled = new CompoundTag();
        styled.putString("text", "styled");
        styled.putBoolean("bold", false);
        var translation = Component.translatable("test.message")
            .arguments(TranslationArgument.bool(true), TranslationArgument.numeric(42), TranslationArgument.component(Component.text("argument")));
        var serializer = DefaultComponentSerializer.nbt();
        var mixed = HeterogeneousNbtList.of(new StringTag("first"), styled,
            HeterogeneousNbtList.of(new StringTag("nested"), new StringTag("child")));
        var tags = List.of(new StringTag("plain\u0000text 🐈"), styled, serializer.serialize(translation), mixed);
        for (var tag : tags) {
            var buffer = Unpooled.buffer();
            try {
                MinecraftTypes.writeTag(buffer, tag);
                buffer.writeInt(0x12345678);
                assertEquals(serializer.deserialize(tag), MinecraftTypes.readComponent(buffer));
                assertEquals(0x12345678, buffer.readInt());
                assertFalse(buffer.isReadable());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void rejectsInvalidComponentTagsAndOversizedNbt() {
        var tags = List.of(new IntTag(42), new ListTag<Tag>(List.of()));
        for (var tag : tags) {
            var buffer = Unpooled.buffer();
            try {
                MinecraftTypes.writeTag(buffer, tag);
                assertThrows(IllegalArgumentException.class, () -> MinecraftTypes.readComponent(buffer));
            } finally {
                buffer.release();
            }
        }
        var buffer = Unpooled.buffer();
        try {
            buffer.writeByte(0);
            assertThrows(IllegalArgumentException.class, () -> MinecraftTypes.readComponent(buffer));
            buffer.clear();
            buffer.writeByte(7); // Byte array with a declared size exceeding the component NBT limit.
            buffer.writeInt(2097153);
            assertThrows(IllegalArgumentException.class, () -> MinecraftTypes.readComponent(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void preservesOpenNbtHoverDataAndDialogsThroughNetworkRoundTrip() {
        var customData = new CompoundTag();
        customData.putString("test", "value");
        var dialog = new CompoundTag();
        dialog.putString("type", "minecraft:notice");
        var components = List.of(
            Component.text("item").hoverEvent(HoverEvent.showItem(Key.key("diamond_sword"), 2, Map.of(
                Key.key("damage"), new NbtDataComponentValue(new IntTag(50)),
                Key.key("custom_data"), new NbtDataComponentValue(customData),
                Key.key("item_name"), DataComponentValue.removed()))),
            Component.text("inline dialog").clickEvent(ClickEvent.showDialog(new NbtDialog(dialog))),
            Component.text("dialog reference").clickEvent(ClickEvent.showDialog(new NbtDialog(Key.key("test:dialog")))),
            Component.text("custom action").clickEvent(ClickEvent.custom(Key.key("test:action")))
        );
        for (var component : components) {
            var buffer = Unpooled.buffer();
            try {
                MinecraftTypes.writeComponent(buffer, component);
                assertEquals(component, MinecraftTypes.readComponent(buffer));
                assertFalse(buffer.isReadable());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void retainsRecipesAsUnnamedBinaryNbt() {
        var recipe = new CompoundTag();
        recipe.putString("id", "minecraft:test_recipe");
        recipe.put("ingredients", new ListTag<>(List.of(new StringTag("minecraft:stone"))));
        var recipes = new ListTag<>(List.of(recipe));
        var binary = MNBTIO.write(recipes, false);
        var buffer = Unpooled.buffer();
        try {
            ItemTypes.writeRecipes(buffer, binary);
            buffer.writeByte(123);
            var decoded = DataComponentTypes.RECIPES.readDataComponent(buffer).getValue();
            assertFalse(decoded.isNamed());
            assertArrayEquals(binary.getData(), decoded.getData());
            assertEquals(recipes, MNBTIO.read(decoded));
            assertEquals(123, buffer.readUnsignedByte());
            buffer.clear();
            ItemTypes.writeRecipes(buffer, decoded);
            var bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            assertArrayEquals(binary.getData(), bytes);
            buffer.clear();
            ItemTypes.writeRecipes(buffer, null);
            assertNull(ItemTypes.readRecipes(buffer));
            assertFalse(buffer.isReadable());
            buffer.clear();
            MinecraftTypes.writeTag(buffer, new CompoundTag());
            assertThrows(IllegalArgumentException.class, () -> ItemTypes.readRecipes(buffer));
        } finally {
            buffer.release();
        }
    }
}
