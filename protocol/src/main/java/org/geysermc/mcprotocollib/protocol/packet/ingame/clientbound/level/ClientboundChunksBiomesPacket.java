package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkBiomeData;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ClientboundChunksBiomesPacket implements MinecraftPacket {
    private final List<ChunkBiomeData> chunkBiomeData;

    public ClientboundChunksBiomesPacket(ByteBuf in, MinecraftCodecHelper helper) {
        int length = helper.readVarInt(in);
        this.chunkBiomeData = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            long raw = in.readLong();
            this.chunkBiomeData.add(new ChunkBiomeData((int)raw, (int)(raw >> 32), helper.readByteArray(in)));
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.chunkBiomeData.size());
        for (int i = 0; i < this.chunkBiomeData.size(); i++) {
            ChunkBiomeData entry = this.chunkBiomeData.get(i);
            long raw = (long)entry.getX() & 0xFFFFFFFFL | ((long)entry.getZ() & 0xFFFFFFFFL) << 32;
            out.writeLong(raw);
            helper.writeByteArray(out, entry.getBuffer());
        }
    }
}
