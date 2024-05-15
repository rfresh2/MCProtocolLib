package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.viaversion.nbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

@Data
@With
@AllArgsConstructor
@ToString(exclude = {"sections", "heightMaps", "lightData"})
public class ClientboundLevelChunkWithLightPacket implements MinecraftPacket {
    private final int x;
    private final int z;
    private @NonNull ChunkSection[] sections;
    private final @NonNull MNBT heightMaps;
    private final @NonNull BlockEntityInfo[] blockEntities;
    private final @NonNull LightUpdateData lightData;

    public ClientboundLevelChunkWithLightPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.x = in.readInt();
        this.z = in.readInt();
        this.heightMaps = helper.readMNBT(in);
        var dataLen = helper.readVarInt(in); // unused
        var sectionCountProvider = MinecraftConstants.CHUNK_SECTION_COUNT_PROVIDER;
        if (sectionCountProvider == null) throw new RuntimeException("Chunk section count provider is null.");
        var sectionCount = sectionCountProvider.getSectionCount();
        this.sections = new ChunkSection[sectionCount];
        for (int i = 0; i < sectionCount; i++) {
            this.sections[i] = helper.readChunkSection(in);
        }

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
