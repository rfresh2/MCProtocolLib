package org.geysermc.mcprotocollib.protocol.data;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;
import net.kyori.option.OptionSchema;

public final class DefaultComponentSerializer {
    private static GsonComponentSerializer serializer = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
        .options(
            OptionSchema.globalSchema().stateBuilder()
                // after 1.16
                .value(JSONOptions.EMIT_RGB, Boolean.TRUE)
                .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.CAMEL_CASE)
                .value(JSONOptions.EMIT_CLICK_EVENT_TYPE, JSONOptions.ClickEventValueMode.CAMEL_CASE)
                // after 1.20.3
                .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.TRUE)
                .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.TRUE)
                .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.TRUE)
                .build()
        )
        .build();

    public static GsonComponentSerializer get() {
        return serializer;
    }

    public static void set(GsonComponentSerializer serializer) {
        DefaultComponentSerializer.serializer = serializer;
    }

    private DefaultComponentSerializer() {
    }
}
