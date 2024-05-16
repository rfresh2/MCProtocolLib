package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkBiomeData;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;

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
            long position = in.readLong();
            int x = (int)position;
            int z = (int)(position >> 32);
            var dataLen = helper.readVarInt(in); // unused
            var sectionCount = MinecraftConstants.CHUNK_SECTION_COUNT_PROVIDER.getSectionCount();
            var palettes = new DataPalette[sectionCount]; // indexed to corresponding chunk section
            for (int j = 0; j < sectionCount; j++) {
                palettes[j] = helper.readDataPalette(in, PaletteType.BIOME);
            }

            this.chunkBiomeData.add(new ChunkBiomeData(x, z, palettes));
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.chunkBiomeData.size());
        for (int i = 0; i < this.chunkBiomeData.size(); i++) {
            ChunkBiomeData entry = this.chunkBiomeData.get(i);
            long position = (long)entry.getX() & 0xFFFFFFFFL | ((long)entry.getZ() & 0xFFFFFFFFL) << 32;
            out.writeLong(position);
            out.markWriterIndex();
            out.writeMedium(0);
            var start = out.writerIndex();
            var sectionCount = MinecraftConstants.CHUNK_SECTION_COUNT_PROVIDER.getSectionCount();
            var palettes = entry.getPalettes();
            for (int j = 0; j < sectionCount; j++) {
                helper.writeDataPalette(out, palettes[j]);
            }
            var end = out.writerIndex();
            var len = end - start;
            out.resetWriterIndex();
            var lenVarInt = (len & 0x7F | 0x80) << 16 | ((len >>> 7) & 0x7F | 0x80) << 8 | (len >>> 14);
            out.writeMedium(lenVarInt); // write actual biome data length over dummy bytes
            out.writerIndex(end);
        }
    }
}
