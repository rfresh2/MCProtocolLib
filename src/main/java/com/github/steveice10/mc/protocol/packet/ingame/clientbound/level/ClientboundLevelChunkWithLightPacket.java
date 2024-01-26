package com.github.steveice10.mc.protocol.packet.ingame.clientbound.level;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.data.game.level.LightUpdateData;
import com.github.steveice10.mc.protocol.data.game.level.block.BlockEntityInfo;
import com.github.steveice10.mc.protocol.data.game.level.block.BlockEntityType;
import com.github.steveice10.opennbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.*;

import java.io.IOException;

@Data
@With
@AllArgsConstructor
@ToString(exclude = {"chunkData", "heightMaps", "lightData"})
public class ClientboundLevelChunkWithLightPacket implements MinecraftPacket {
    private final int x;
    private final int z;
    private final @NonNull byte[] chunkData;
    private final @NonNull MNBT heightMaps;
    private final @NonNull BlockEntityInfo[] blockEntities;
    private final @NonNull LightUpdateData lightData;

    public ClientboundLevelChunkWithLightPacket(ByteBuf in, MinecraftCodecHelper helper) throws IOException {
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
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) throws IOException {
        out.writeInt(this.x);
        out.writeInt(this.z);
        helper.writeMNBT(out, this.heightMaps);
        helper.writeVarInt(out, this.chunkData.length);
        out.writeBytes(this.chunkData);

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
