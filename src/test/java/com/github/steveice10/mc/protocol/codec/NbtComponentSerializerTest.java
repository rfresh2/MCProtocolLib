package com.github.steveice10.mc.protocol.codec;

import com.github.steveice10.mc.protocol.data.DefaultComponentSerializer;
import com.github.steveice10.opennbt.tag.io.MNBTIO;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Assertions;

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
