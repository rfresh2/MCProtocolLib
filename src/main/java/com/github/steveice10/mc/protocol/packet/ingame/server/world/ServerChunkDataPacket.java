package com.github.steveice10.mc.protocol.packet.ingame.server.world;

import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.mc.protocol.util.NetUtil;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import io.netty.buffer.Unpooled;

import java.io.IOException;

public class ServerChunkDataPacket extends MinecraftPacket {
    private Column column;

    public ServerChunkDataPacket(NetInput in) throws IOException {
        int x = in.readInt();
        int z = in.readInt();
        boolean fullChunk = in.readBoolean();
        int chunkMask = in.readVarInt();
        byte[] data = in.readBytes(in.readVarInt());
        CompoundTag[] tileEntities = new CompoundTag[in.readVarInt()];
        for(int i = 0; i < tileEntities.length; i++) {
            tileEntities[i] = NetUtil.readNBT(in);
        }

        this.column = NetUtil.readColumn(data, x, z, fullChunk, false, chunkMask, tileEntities);
    }

    public ServerChunkDataPacket(Column column) {
        this.column = column;
    }

    public Column getColumn() {
        return this.column;
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeInt(this.column.getX());
        out.writeInt(this.column.getZ());
        out.writeBoolean(this.column.hasBiomeData());
        int columnSize = this.column.getSerializedSize();
        final byte[] buffer = new byte[columnSize];
        final ByteBufNetOutput writeBuffer = new ByteBufNetOutput(Unpooled.wrappedBuffer(buffer));
        writeBuffer.getBuffer().resetWriterIndex();
        int mask = NetUtil.writeColumn(writeBuffer, this.column, this.column.hasBiomeData(), this.column.hasSkylight());
        out.writeVarInt(mask);
        out.writeVarInt(columnSize);
        out.writeBytes(buffer);
        CompoundTag[] tileEntitiesTags = this.column.getTileEntitiesTags();
        out.writeVarInt(tileEntitiesTags.length);
        for(CompoundTag tag : tileEntitiesTags) {
            NetUtil.writeNBT(out, tag);
        }
    }
}
