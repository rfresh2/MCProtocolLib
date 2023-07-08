package com.github.steveice10.mc.protocol.packet.ingame.server.world;

import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.mc.protocol.util.NetUtil;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ServerChunkDataPacket extends MinecraftPacket {
    private final int x;
    private final int z;
    private final int availableSections;
    private final byte[] data;
    private final CompoundTag[] tileEntityTags;
    private final boolean fullChunk;

    public ServerChunkDataPacket(NetInput in) throws IOException {
        this.x = in.readInt();
        this.z = in.readInt();
        this.fullChunk = in.readBoolean();
        this.availableSections = in.readVarInt();
        int size = in.readVarInt();
        if (size > 0x200000) {
            throw new IOException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.data = in.readBytes(size);
        CompoundTag[] tileEntities = new CompoundTag[in.readVarInt()];
        for(int i = 0; i < tileEntities.length; i++) {
            tileEntities[i] = NetUtil.readNBT(in);
        }
        this.tileEntityTags = tileEntities;
    }

    public ServerChunkDataPacket(int x, int z, int availableSections, byte[] data, CompoundTag[] tileEntityTags, boolean fullChunk) {
        this.x = x;
        this.z = z;
        this.availableSections = availableSections;
        this.data = data;
        this.tileEntityTags = tileEntityTags;
        this.fullChunk = fullChunk;
    }

    public ServerChunkDataPacket(Column column) {
        this.x = column.getX();
        this.z = column.getZ();
        this.fullChunk = column.hasBiomeData();
        int size = column.getSerializedSize();
        if (size > 0x200000) {
            throw new IllegalArgumentException("Cannot send packet larger than 2M");
        }
        byte[] bytes = new byte[size];
        ByteBufNetOutput buf = ByteBufNetOutput.createWrappedOutput(bytes);
        try {
            this.availableSections = NetUtil.writeColumn(buf, column, this.fullChunk, column.hasSkylight());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        this.data = bytes;
        this.tileEntityTags = column.getTileEntitiesTags();
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeInt(this.x);
        out.writeInt(this.z);
        out.writeBoolean(this.fullChunk);
        out.writeVarInt(this.availableSections);
        out.writeVarInt(this.data.length);
        out.writeBytes(this.data);
        out.writeVarInt(this.tileEntityTags.length);
        for(CompoundTag tag : this.tileEntityTags) {
            NetUtil.writeNBT(out, tag);
        }
    }

    /**
     * Only the overworld has skylight. This prevents us from doing unnecessary repeated work to determine if a chunk has skylight.q
     */
    public Column readColumn(boolean hasSkylight) {
        try {
            return NetUtil.readColumn(this.data, this.x, this.z, this.fullChunk, hasSkylight, this.availableSections, this.tileEntityTags);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read column without knowing if it has skylight. will cause repeated read if its in the overworld.
     **/
    public Column readColumn() {
        try {
            return NetUtil.readColumnWithUnknownSkylight(this.data, this.x, this.z, this.fullChunk, false, this.availableSections, this.tileEntityTags);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
