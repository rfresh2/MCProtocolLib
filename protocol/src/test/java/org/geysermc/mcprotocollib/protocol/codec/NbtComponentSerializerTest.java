package org.geysermc.mcprotocollib.protocol.codec;

import com.viaversion.nbt.io.MNBTIO;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import org.geysermc.mcprotocollib.protocol.data.DefaultComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class NbtComponentSerializerTest {
    //    @Test
//    public void emptyComponentTest() {
//        componentEqualityTest(Component.empty());
//        binaryTest(Component.empty());
//    }
//
//    @Test
//    public void testComponentTest() {
//        componentEqualityTest(Component.text("hello from component"));
//        binaryTest(Component.text("hello from component"));
//    }
//
//    @Test
//    public void clickEventComponentTest() {
//        componentEqualityTest(Component.text("hello from component").clickEvent(clickEvent(ClickEvent.Action.OPEN_URL, "https://example.com")));
//        binaryTest(Component.text("hello from component").clickEvent(clickEvent(ClickEvent.Action.OPEN_URL, "https://example.com")));
//    }
//
//    @Test
//    public void styledComponentTest() {
//        componentEqualityTest(Component.text("hello from component").color(net.kyori.adventure.text.format.NamedTextColor.RED));
//        binaryTest(Component.text("hello from component").color(net.kyori.adventure.text.format.NamedTextColor.RED));
//    }

    @Test
    public void buildHeightTest() {
        var json = "{\"color\":\"red\",\"translate\":\"build.tooHigh\",\"with\":[319]}";
        var component = DefaultComponentSerializer.get().deserialize(json);
        componentEqualityTest(component);
        binaryTest(component);
    }

//    @Test
    public void playerSleepTest() {
        var json = "{\"translate\":\"sleep.players_sleeping\",\"with\":[1,2]}";
        var component = DefaultComponentSerializer.get().deserialize(json);
        componentEqualityTest(component);
        binaryTest(component);
    }

//    @Test
    public void shadowColorTest() {
        var json = "{\"color\":\"black\",\"shadow_color\":1694498815,\"text\":\"hello\"}";
        var component = DefaultComponentSerializer.get().deserialize(json);
        componentEqualityTest(component);
        binaryTest(component);
    }

//    @Test
    public void objectComponentTest() {
        var component = Component.text("test").append(Component.object(b -> b.contents(ObjectContents.playerHead(UUID.randomUUID())).fallback(Component.text("fallbackText").append(Component.text(2)))));
        var json = DefaultComponentSerializer.get().serialize(component);
//        componentEqualityTest(component);
        var mnbt = BinaryNbtComponentSerializer.serializeToMNBT(component);
        var tag = MNBTIO.read(mnbt);
    }

    @Test
    public void nbtComponentTest() {
        var component = Component.blockNBT("the_nbt_path", true, Component.text("-"), BlockNBTComponent.Pos.fromString("100 200 300"))
            .append(Component.entityNBT("entity_nbt_path", "minecraft:player"))
            .append(Component.storageNBT("storage_nbt_path", true, Key.key("minecraft:chest")));
        var json = DefaultComponentSerializer.get().serialize(component);
        var mnbt = BinaryNbtComponentSerializer.serializeToMNBT(component);
        var tag = MNBTIO.read(mnbt);
    }

//    @Test
//    public void translatableArgsTest() {
//        var component = Component.translatable()
//            .key("argument.block.property.unknown")
//            .arguments(Component.text("stone"), Component.text("someProperty"))
//            .color(NamedTextColor.RED)
//            .asComponent();
//        componentEqualityTest(component);
//        // seems to work in mc client even if this test fails
//        binaryTest(component);
//    }

    private void componentEqualityTest(Component component) {
        var nbt = NbtComponentSerializer.jsonComponentToTag(DefaultComponentSerializer.get().serializeToTree(component));

        var json = NbtComponentSerializer.tagComponentToJson(nbt);
        var parsedComponent = DefaultComponentSerializer.get().deserializeFromTree(json);

        Assertions.assertEquals(component, parsedComponent);
    }

    @SneakyThrows
    private void binaryTest(Component component) {
        var nbt = NbtComponentSerializer.jsonComponentToTag(DefaultComponentSerializer.get().serializeToTree(component));
        var mnbt = BinaryNbtComponentSerializer.serializeToMNBT(component);

        var mnbtTag = MNBTIO.read(mnbt);

        Assertions.assertEquals(nbt, mnbtTag);
    }

//    private void mnbtComponentEqualityTest(Component component) {
//        var nbt = BinaryNbtComponentSerializer.serializeToMNBT(component);
//
//        var json = BinaryNbtComponentSerializer.(nbt);
//        var parsedComponent = DefaultComponentSerializer.get().deserializeFromTree(json);
//
//        Assertions.assertEquals(component, parsedComponent);
//    }
}
