package com.github.steveice10.mc.protocol.data;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;

public final class DefaultComponentSerializer {
    private static GsonComponentSerializer serializer = GsonComponentSerializer.builder()
            .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
            .options(JSONOptions.byDataVersion().at(2526))
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
