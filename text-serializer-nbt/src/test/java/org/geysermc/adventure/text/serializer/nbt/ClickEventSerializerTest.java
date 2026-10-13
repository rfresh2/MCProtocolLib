package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

public class ClickEventSerializerTest {
    static final List<Arguments> CLICK_EVENTS = List.of(
        Arguments.arguments(
            TestTags.compound("action", "open_url", "url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
            ClickEvent.openUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        ),
        Arguments.arguments(
            TestTags.compound(
                "action", "open_file",
                "path", "/home/eclipse/Documents/Code/Java/Minecraft/Geyser/Geyser/hidden-cheat-codes"
            ),
            ClickEvent.openFile("/home/eclipse/Documents/Code/Java/Minecraft/Geyser/Geyser/hidden-cheat-codes")
        ),
        Arguments.arguments(
            TestTags.compound("action", "run_command", "command", "give eclipseisoffline diamond 64"),
            ClickEvent.runCommand("give eclipseisoffline diamond 64")
        ),
        Arguments.arguments(
            TestTags.compound("action", "suggest_command", "command", "clear AuriiU diamond 64"),
            ClickEvent.suggestCommand("clear AuriiU diamond 64")
        ),
        Arguments.arguments(
            TestTags.compound("action", "change_page", "page", 5),
            ClickEvent.changePage(5)
        ),
        Arguments.arguments(
            TestTags.compound("action", "copy_to_clipboard", "value", "aGVsbG8gZnJvbSBlY2xpcHNlIDop"),
            ClickEvent.copyToClipboard("aGVsbG8gZnJvbSBlY2xpcHNlIDop")
        ),
        Arguments.arguments(
            TestTags.compound("action", "show_dialog", "dialog", "geyser:login_form"),
            ClickEvent.showDialog(new NbtDialog(Key.key("geyser", "login_form")))
        ),
        Arguments.arguments(
            TestTags.compound("action", "show_dialog", "dialog", new CompoundTag()),
            ClickEvent.showDialog(new NbtDialog(new CompoundTag()))
        ),
        Arguments.arguments(
            TestTags.compound("action", "custom", "id", "geyser:my_payload"),
            ClickEvent.custom(Key.key("geyser", "my_payload"))
        )
        // Payloads are not supported yet
        /*
        Arguments.arguments(
            TestTags.compound(
                "action", "custom",
                "id", "geyser:complicated_payload",
                "payload", TestTags.compound("complicated", "Very much")
            ),
            ClickEvent.custom(Key.key("geyser", "complicated_payload"), new NbtBinaryTagHolder(TestTags.compound("complicated", "Very much")))
        )
         */
    );

    @ParameterizedTest
    @FieldSource("CLICK_EVENTS")
    void testDeserialize(CompoundTag map, ClickEvent<?> result) {
        Assertions.assertEquals(result, ClickEventSerializerImpl.deserialize(map));
    }

    @ParameterizedTest
    @FieldSource("CLICK_EVENTS")
    void testSerialize(CompoundTag result, ClickEvent<?> event) {
        Assertions.assertEquals(result, ClickEventSerializerImpl.serialize(event));
    }

    @Test
    void testDeserializeInvalidDialog() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ClickEventSerializerImpl.deserialize(TestTags.compound("action", "show_dialog", "dialog", 12345)), "decoding a dialog that is not a string or CompoundTag should throw an IllegalArgumentException");
    }

    @Test
    void testSerializeInvalidDialog() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ClickEventSerializerImpl.serialize(ClickEvent.showDialog(new DialogLike() {})),
            "encoding a dialog that is not an NbtDialog should throw an IllegalArgumentException");
    }
}
