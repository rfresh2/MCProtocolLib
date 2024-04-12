package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.github.steveice10.opennbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

@Data
@With
@AllArgsConstructor
public class ClientboundLevelChunkWithLightPacket implements MinecraftPacket {
    private final int x;
    private final int z;
    private final byte @Nullable [] chunkData; // must be non-null after deserialization
    private @Nullable ChunkSection[] sections; // for serialization. preferred if chunkData is null
    private final @NonNull MNBT heightMaps;
    private final @NonNull BlockEntityInfo[] blockEntities;
    private final @NonNull LightUpdateData lightData;

    public ClientboundLevelChunkWithLightPacket(int x, int z, byte[] chunkData, MNBT heightMaps, BlockEntityInfo[] blockEntities, LightUpdateData lightData) {
        this.x = x;
        this.z = z;
        this.chunkData = chunkData;
        this.heightMaps = heightMaps;
        this.blockEntities = blockEntities;
        this.lightData = lightData;
    }

    public ClientboundLevelChunkWithLightPacket(int x, int z, ChunkSection[] sections, MNBT heightMaps, BlockEntityInfo[] blockEntities, LightUpdateData lightData) {
        this.x = x;
        this.z = z;
        this.chunkData = null;
        this.sections = sections;
        this.heightMaps = heightMaps;
        this.blockEntities = blockEntities;
        this.lightData = lightData;
    }

    public ClientboundLevelChunkWithLightPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.x = in.readInt();
        this.z = in.readInt();
        this.heightMaps = helper.readMNBT(in);
        this.chunkData = helper.readByteArray(in);

        this.blockEntities = new BlockEntityInfo[helper.readVarInt(in)];
        for (int i = 0; i < this.blockEntities.length; i++) {
            byte xz = in.readByte();
            int blockEntityX = (xz >> 4) & 15;
            int blockEntityZ = xz & 15;
            int blockEntityY = in.readShort();
            BlockEntityType type = helper.readBlockEntityType(in);
            MNBT tag = helper.readMNBT(in);
            this.blockEntities[i] = new BlockEntityInfo(blockEntityX, blockEntityY, blockEntityZ, type, tag);
        }

        this.lightData = helper.readLightUpdateData(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        out.writeInt(this.x);
        out.writeInt(this.z);
        helper.writeMNBT(out, this.heightMaps);
        if (this.chunkData == null) {
            if (this.sections == null) {
                throw new IllegalStateException("Chunk data and sections are both null.");
            }
            out.markWriterIndex();
            out.writeMedium(0); // Dummy chunk data length varint
            var start = out.writerIndex();
            for (int i = 0; i < this.sections.length; i++) {
                helper.writeChunkSection(out, this.sections[i]);
            }
            var end = out.writerIndex();
            var len = end - start;
            out.resetWriterIndex();
            var lenVarInt = (len & 0x7F | 0x80) << 16 | ((len >>> 7) & 0x7F | 0x80) << 8 | (len >>> 14);
            out.writeMedium(lenVarInt); // write actual chunk data length over dummy bytes
            out.writerIndex(end);
        } else {
            helper.writeVarInt(out, this.chunkData.length);
            out.writeBytes(this.chunkData);
        }

        helper.writeVarInt(out, this.blockEntities.length);
        for (BlockEntityInfo blockEntity : this.blockEntities) {
            out.writeByte(((blockEntity.getX() & 15) << 4) | blockEntity.getZ() & 15);
            out.writeShort(blockEntity.getY());
            helper.writeBlockEntityType(out, blockEntity.getType());
            helper.writeMNBT(out, blockEntity.getNbt());
        }

        helper.writeLightUpdateData(out, this.lightData);
    }
}
