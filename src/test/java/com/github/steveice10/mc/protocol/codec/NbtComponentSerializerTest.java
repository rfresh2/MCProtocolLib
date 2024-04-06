package com.github.steveice10.mc.protocol.codec;

import com.github.steveice10.mc.protocol.data.DefaultComponentSerializer;
import com.github.steveice10.opennbt.tag.io.MNBTIO;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

    @Test
    public void translatableArgsTest() {
        var component = Component.translatable()
            .key("argument.block.property.unknown")
            .arguments(Component.text("stone"), Component.text("someProperty"))
            .color(NamedTextColor.RED)
            .asComponent();
        componentEqualityTest(component);
        // seems to work in mc client even if this test fails
        binaryTest(component);
    }

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
