package com.github.steveice10.mc.protocol.data.game.chunk;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.util.ObjectUtil;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Setter
public class Column {
    private int x;
    private int z;
    private Chunk chunks[];
    private byte biomeData[];
    private List<TileEntity> tileEntities;
    private boolean skylight;

    public Column(int x, int z, Chunk chunks[], CompoundTag[] tileEntities) {
        this(x, z, chunks, null, tileEntities);
    }

    public Column(int x, int z, Chunk chunks[], byte biomeData[], CompoundTag[] tileEntities) {
        if(chunks.length != 16) {
            throw new IllegalArgumentException("Chunk array length must be 16.");
        }

        if(biomeData != null && biomeData.length != 256) {
            throw new IllegalArgumentException("Biome data array length must be 256.");
        }

        this.skylight = false;
        boolean noSkylight = false;
        for(Chunk chunk : chunks) {
            if(chunk != null) {
                if(chunk.getSkyLight() == null) {
                    noSkylight = true;
                } else {
                    this.skylight = true;
                }
            }
        }

        if(noSkylight && this.skylight) {
            throw new IllegalArgumentException("Either all chunks must have skylight values or none must have them.");
        }

        this.x = x;
        this.z = z;
        this.chunks = chunks;
        this.biomeData = biomeData;
        this.tileEntities = tileEntities != null
                ? Collections.synchronizedList(Arrays.stream(tileEntities).map(this::tagToTileEntity).collect(Collectors.toList()))
                : Collections.synchronizedList(new ArrayList<>());
    }

    public Column(int x, int z, Chunk chunks[], byte biomeData[], List<TileEntity> tileEntities) {
        if(chunks.length != 16) {
            throw new IllegalArgumentException("Chunk array length must be 16.");
        }

        if(biomeData != null && biomeData.length != 256) {
            throw new IllegalArgumentException("Biome data array length must be 256.");
        }

        this.skylight = false;
        boolean noSkylight = false;
        for(Chunk chunk : chunks) {
            if(chunk != null) {
                if(chunk.getSkyLight() == null) {
                    noSkylight = true;
                } else {
                    this.skylight = true;
                }
            }
        }

        if(noSkylight && this.skylight) {
            throw new IllegalArgumentException("Either all chunks must have skylight values or none must have them.");
        }

        this.x = x;
        this.z = z;
        this.chunks = chunks;
        this.biomeData = biomeData;
        this.tileEntities = Collections.synchronizedList(tileEntities);
    }


    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public Chunk[] getChunks() {
        return this.chunks;
    }

    public boolean hasBiomeData() {
        return this.biomeData != null;
    }

    public byte[] getBiomeData() {
        return this.biomeData;
    }

    public List<TileEntity> getTileEntities() {
        return this.tileEntities;
    }
    public CompoundTag[] getTileEntitiesTags() {
        synchronized (this.tileEntities) {
            return this.tileEntities.stream().map(TileEntity::getCompoundTag).toArray(CompoundTag[]::new);
        }
    }

    public void setTileEntities(final CompoundTag[] compoundTags) {
        synchronized (this.tileEntities) {
            this.tileEntities = Collections.synchronizedList(Arrays.stream(compoundTags).map(this::tagToTileEntity).collect(Collectors.toList()));
        }
    }

    public boolean hasSkylight() {
        return this.skylight;
    }

    private TileEntity tagToTileEntity(CompoundTag tag) {
        try {
            final Position position = new Position(tag.<IntTag>get("x").getValue(), tag.<IntTag>get("y").getValue(), tag.<IntTag>get("z").getValue());
            return new TileEntity(position, tag);
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return getX() == column.getX() && getZ() == column.getZ() && skylight == column.skylight && Arrays.equals(getChunks(), column.getChunks()) && Arrays.equals(getBiomeData(), column.getBiomeData()) && Objects.equals(getTileEntities(), column.getTileEntities());
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(this.x, this.z, this.chunks, this.biomeData, this.tileEntities);
    }

    @Override
    public String toString() {
        return ObjectUtil.toString(this);
    }
}
