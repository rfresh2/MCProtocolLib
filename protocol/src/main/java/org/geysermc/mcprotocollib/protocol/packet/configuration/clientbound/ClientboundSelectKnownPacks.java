package org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.KnownPack;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundSelectKnownPacks implements MinecraftPacket {
    private final List<KnownPack> knownPacks;

    public ClientboundSelectKnownPacks(ByteBuf in, MinecraftCodecHelper helper) {
        int entryCount = helper.readVarInt(in);
        this.knownPacks = new ArrayList<>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            this.knownPacks.add(new KnownPack(helper.readString(in), helper.readString(in), helper.readString(in)));
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.knownPacks.size());
        for (int i = 0; i < this.knownPacks.size(); i++) {
            KnownPack entry = this.knownPacks.get(i);
            helper.writeString(out, entry.getNamespace());
            helper.writeString(out, entry.getId());
            helper.writeString(out, entry.getVersion());
        }
    }
}
