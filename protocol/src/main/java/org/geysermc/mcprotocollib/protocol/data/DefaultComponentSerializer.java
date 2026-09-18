package org.geysermc.mcprotocollib.protocol.data;

import lombok.Setter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;
import org.geysermc.adventure.text.serializer.nbt.NbtComponentSerializer;

public final class DefaultComponentSerializer {
    private static GsonComponentSerializer gson = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
        // From net.minecraft.SharedConstants.WORLD_VERSION
        .options(JSONOptions.byDataVersion().at(5023))
        .build();
    @Setter
    private static NbtComponentSerializer nbt = NbtComponentSerializer.nbt();

    public static GsonComponentSerializer get() {
        return gson;
    }

    public static void set(GsonComponentSerializer serializer) {
        gson = serializer;
    }

    public static NbtComponentSerializer nbt() {
        return nbt;
    }

    private DefaultComponentSerializer() {}
}
