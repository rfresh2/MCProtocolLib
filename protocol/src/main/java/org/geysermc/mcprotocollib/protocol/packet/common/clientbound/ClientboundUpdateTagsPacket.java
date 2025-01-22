package org.geysermc.mcprotocollib.protocol.packet.common.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.HashMap;
import java.util.Map;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "tags")
public class ClientboundUpdateTagsPacket implements MinecraftPacket {
    private final @NonNull Map<String, Map<String, int[]>> tags;

    public ClientboundUpdateTagsPacket(ByteBuf in) {
        int totalTagCount = MinecraftTypes.readVarInt(in);
        this.tags = new HashMap<>(totalTagCount);
        for (int i = 0; i < totalTagCount; i++) {
            String tagName = MinecraftTypes.readResourceLocationString(in);
            int tagsCount = MinecraftTypes.readVarInt(in);
            Map<String, int[]> tag = new HashMap<>(tagsCount);
            for (int j = 0; j < tagsCount; j++) {
                String name = MinecraftTypes.readResourceLocationString(in);
                int entriesCount = MinecraftTypes.readVarInt(in);
                int[] entries = new int[entriesCount];
                for (int index = 0; index < entriesCount; index++) {
                    entries[index] = MinecraftTypes.readVarInt(in);
                }

                tag.put(name, entries);
            }
            tags.put(tagName, tag);
        }
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, tags.size());
        for (Map.Entry<String, Map<String, int[]>> tagSet : tags.entrySet()) {
            MinecraftTypes.writeResourceLocation(out, tagSet.getKey());
            MinecraftTypes.writeVarInt(out, tagSet.getValue().size());
            for (Map.Entry<String, int[]> tag : tagSet.getValue().entrySet()) {
                MinecraftTypes.writeResourceLocation(out, tag.getKey());
                MinecraftTypes.writeVarInt(out, tag.getValue().length);
                for (int id : tag.getValue()) {
                    MinecraftTypes.writeVarInt(out, id);
                }
            }
        }
    }
}
