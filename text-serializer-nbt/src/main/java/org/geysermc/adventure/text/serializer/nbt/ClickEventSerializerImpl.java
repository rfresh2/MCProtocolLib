package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Optional;

final class ClickEventSerializerImpl {

    static ClickEvent<?> deserialize(CompoundTag map) {
        var action = ClickEvent.Action.NAMES.valueOrThrow(map.getString("action"));

        // switch case will intentionally break compile with new actions
        return switch (action) {
            case ClickEvent.Action.OpenUrl ignored -> ClickEvent.openUrl(map.getString("url"));
            case ClickEvent.Action.OpenFile ignored -> ClickEvent.openFile(map.getString("path"));
            case ClickEvent.Action.RunCommand ignored -> ClickEvent.runCommand(map.getString("command"));
            case ClickEvent.Action.SuggestCommand ignored -> ClickEvent.suggestCommand(map.getString("command"));
            case ClickEvent.Action.ChangePage ignored -> ClickEvent.changePage(map.getInt("page"));
            case ClickEvent.Action.CopyToClipboard ignored -> ClickEvent.copyToClipboard(map.getString("value"));
            case ClickEvent.Action.ShowDialog ignored -> {
                var tag = map.get("dialog");
                if (tag instanceof StringTag reference) {
                    yield ClickEvent.showDialog(new NbtDialog(Key.key(reference.getValue())));
                } else if (tag instanceof CompoundTag inline) {
                    yield ClickEvent.showDialog(new NbtDialog(inline));
                }
                throw new IllegalArgumentException("Expected \"dialog\" of \"show_dialog\" click event to be a string reference or compound tag, got: " + tag);
            }
            // Note: not decoding payload!
            case ClickEvent.Action.Custom ignored -> ClickEvent.custom(Key.key(map.getString("id")));
        };
    }

    static CompoundTag serialize(ClickEvent<?> event) {
        var builder = new CompoundTag();
        builder.putString("action", event.action().name());
        var payload = event.payload();

        // switch case will intentionally break compile with new actions
        switch (event.action()) {
            case ClickEvent.Action.OpenUrl ignored -> builder.putString("url", asText(payload));
            case ClickEvent.Action.OpenFile ignored -> builder.putString("path", asText(payload));
            case ClickEvent.Action.RunCommand ignored -> builder.putString("command", asText(payload));
            case ClickEvent.Action.SuggestCommand ignored -> builder.putString("command", asText(payload));
            case ClickEvent.Action.ChangePage ignored -> builder.putInt("page", asInt(payload));
            case ClickEvent.Action.CopyToClipboard ignored -> builder.putString("value", asText(payload));
            case ClickEvent.Action.ShowDialog ignored -> {
                var dialogPayload = (ClickEvent.Payload.Dialog) payload;
                if (dialogPayload.dialog() instanceof NbtDialog(Optional<Key> reference, Optional<CompoundTag> inline)) {
                    reference.ifPresent(key -> builder.putString("dialog", key.asString()));
                    inline.ifPresent(map -> builder.put("dialog", map));
                } else {
                    throw new IllegalArgumentException("Unable to encode \"show_dialog\" click event with DialogLike that is not an NbtDialog");
                }
            }
            case ClickEvent.Action.Custom ignored -> {
                var customPayload = (ClickEvent.Payload.Custom) payload;
                builder.putString("id", customPayload.key().asString());

                // Note: not encoding payload!
            }
        }
        return builder;
    }

    private static String asText(ClickEvent.Payload payload) {
        return ((ClickEvent.Payload.Text) payload).value();
    }

    private static int asInt(ClickEvent.Payload payload) {
        return ((ClickEvent.Payload.Int) payload).integer();
    }
}
