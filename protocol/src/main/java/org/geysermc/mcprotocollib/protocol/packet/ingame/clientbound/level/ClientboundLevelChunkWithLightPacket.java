package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.viaversion.nbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.HeightmapTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

import java.util.EnumMap;
import java.util.Map;

@Data
@With
@AllArgsConstructor
@ToString(exclude = {"sections", "heightMaps", "lightData"})
public class ClientboundLevelChunkWithLightPacket implements MinecraftPacket {
    private final int x;
    private final int z;
    private @NonNull ChunkSection[] sections;
    private final @NonNull Map<HeightmapTypes, long[]> heightMaps;
    private final @NonNull BlockEntityInfo[] blockEntities;
    private final @NonNull LightUpdateData lightData;

    public ClientboundLevelChunkWithLightPacket(ByteBuf in) {
        this.x = in.readInt();
        this.z = in.readInt();

        this.heightMaps = new EnumMap<>(HeightmapTypes.class);
        int length = MinecraftTypes.readVarInt(in);
        for (int i = 0; i < length; i++) {
            this.heightMaps.put(HeightmapTypes.from(MinecraftTypes.readVarInt(in)), MinecraftTypes.readLongArray(in));
        }

        var dataLen = MinecraftTypes.readVarInt(in); // unused
        var sectionCountProvider = MinecraftConstants.CHUNK_SECTION_COUNT_PROVIDER;
        if (sectionCountProvider == null) throw new RuntimeException("Chunk section count provider is null.");
        var sectionCount = sectionCountProvider.getSectionCount();
        this.sections = new ChunkSection[sectionCount];
        for (int i = 0; i < sectionCount; i++) {
            this.sections[i] = MinecraftTypes.readChunkSection(in);
        }

        var blockEntityCount = MinecraftTypes.readVarInt(in);
        this.blockEntities = new BlockEntityInfo[blockEntityCount];
        for (int i = 0; i < blockEntityCount; i++) {
            byte xz = in.readByte();
            int blockEntityX = (xz >> 4) & 15;
            int blockEntityZ = xz & 15;
            int blockEntityY = in.readShort();
            BlockEntityType type = MinecraftTypes.readBlockEntityType(in);
            MNBT tag = MinecraftTypes.readMNBT(in);
            this.blockEntities[i] = new BlockEntityInfo(blockEntityX, blockEntityY, blockEntityZ, type, tag);
        }

        this.lightData = MinecraftTypes.readLightUpdateData(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeInt(this.x);
        out.writeInt(this.z);

        MinecraftTypes.writeVarInt(out, this.heightMaps.size());
        for (Map.Entry<HeightmapTypes, long[]> entry : this.heightMaps.entrySet()) {
            MinecraftTypes.writeVarInt(out, entry.getKey().ordinal());
            MinecraftTypes.writeLongArray(out, entry.getValue());
        }

        out.markWriterIndex();
        out.writeMedium(0); // Dummy chunk data length varint
        var start = out.writerIndex();
        for (int i = 0; i < this.sections.length; i++) {
            MinecraftTypes.writeChunkSection(out, this.sections[i]);
        }
        var end = out.writerIndex();
        var len = end - start;
        out.resetWriterIndex();
        var lenVarInt = (len & 0x7F | 0x80) << 16 | ((len >>> 7) & 0x7F | 0x80) << 8 | (len >>> 14);
        out.writeMedium(lenVarInt); // write actual chunk data length over dummy bytes
        out.writerIndex(end);

        MinecraftTypes.writeVarInt(out, this.blockEntities.length);
        for (int i = 0; i < this.blockEntities.length; i++) {
            var blockEntity = this.blockEntities[i];
            out.writeByte(((blockEntity.getX() & 15) << 4) | blockEntity.getZ() & 15);
            out.writeShort(blockEntity.getY());
            MinecraftTypes.writeBlockEntityType(out, blockEntity.getType());
            MinecraftTypes.writeMNBT(out, blockEntity.getNbt());
        }

        MinecraftTypes.writeLightUpdateData(out, this.lightData);
    }
}
