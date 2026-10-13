package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

public class StyleSerializerTest {
    static final List<Arguments> STYLES = List.of(
        Arguments.arguments(
            TestTags.compound("color", "dark_purple"),
            Style.style().color(NamedTextColor.DARK_PURPLE)
        ),
        Arguments.arguments(
            TestTags.compound("color", "#AACCEE"),
            Style.style().color(TextColor.fromHexString("#AACCEE"))
        ),
        Arguments.arguments(
            TestTags.compound("shadow_color", 0xAABBCCDD),
            Style.style().shadowColor(ShadowColor.shadowColor(0xAABBCCDD))
        ),
        Arguments.arguments(
            TestTags.compound("bold", true),
            Style.style().decorate(TextDecoration.BOLD)
        ),
        Arguments.arguments(
            TestTags.compound("bold", true, "italic", true, "obfuscated", true),
            Style.style().decorate(TextDecoration.BOLD, TextDecoration.ITALIC, TextDecoration.OBFUSCATED)
        ),
        Arguments.arguments(
            TestTags.compound(
                "bold", true,
                "italic", false,
                "underlined", false,
                "strikethrough", false,
                "obfuscated", true
            ),
            Style.style()
                .decorate(TextDecoration.BOLD, TextDecoration.OBFUSCATED)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.UNDERLINED, false)
                .decoration(TextDecoration.STRIKETHROUGH, false)
        ),
        Arguments.arguments(
            TestTags.compound(
                "click_event", TestTags.compound("action", "open_url", "url", "https://www.youtube.com/watch?v=_yqSbnbUsj4")
            ),
            Style.style().clickEvent(ClickEvent.openUrl("https://www.youtube.com/watch?v=_yqSbnbUsj4"))
        ),
        Arguments.arguments(
            TestTags.compound("hover_event", TestTags.compound("action", "show_text", "value", "I love cats.")),
            Style.style().hoverEvent(HoverEvent.showText(Component.text("I love cats.")))
        ),
        Arguments.arguments(
            TestTags.compound("insertion", "h"),
            Style.style().insertion("h")
        ),
        Arguments.arguments(
            TestTags.compound("font", "geyser:my_super_cool_font"),
            Style.style().font(Key.key("geyser", "my_super_cool_font"))
        ),
        Arguments.arguments(
            TestTags.compound(
                "color", "dark_aqua",
                "shadow_color", 0x12345678,
                "bold", false,
                "italic", true,
                "underlined", true,
                "strikethrough", true,
                "obfuscated", false,
                "click_event", TestTags.compound("action", "open_url", "url", "https://www.youtube.com/watch?v=FtutLA63Cp8"),
                "hover_event", TestTags.compound("action", "show_text", "value", "yummy"),
                "insertion", ",",
                "font", "geyser:less_cool_font"
            ),
            Style.style()
                .color(NamedTextColor.DARK_AQUA)
                .shadowColor(ShadowColor.shadowColor(0x12345678))
                .decoration(TextDecoration.BOLD, false)
                .decoration(TextDecoration.ITALIC, true)
                .decoration(TextDecoration.UNDERLINED, true)
                .decoration(TextDecoration.STRIKETHROUGH, true)
                .decoration(TextDecoration.OBFUSCATED, false)
                .clickEvent(ClickEvent.openUrl("https://www.youtube.com/watch?v=FtutLA63Cp8"))
                .hoverEvent(HoverEvent.showText(Component.text("yummy")))
                .insertion(",")
                .font(Key.key("geyser", "less_cool_font"))
        )
    );

    @ParameterizedTest
    @FieldSource("STYLES")
    void testDeserialize(CompoundTag map, Style.Builder result) {
        Assertions.assertEquals(result.build(), StyleSerializerImpl.deserialize(map, NbtComponentSerializer.nbt()));
    }

    @ParameterizedTest
    @FieldSource("STYLES")
    void testSerialize(CompoundTag result, Style.Builder style) {
        var builder = TestTags.compound();
        StyleSerializerImpl.serialize(builder, style.build(), NbtComponentSerializer.nbt());
        Assertions.assertEquals(result, builder);
    }
}
