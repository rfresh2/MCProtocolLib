package org.geysermc.mcprotocollib.protocol.data.game.chunk;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class ChunkSection {

    private static final int AIR = 0;

    private int blockCount;
    private int fluidCount;
    private @NonNull DataPalette chunkData;
    private @NonNull DataPalette biomeData;

    public ChunkSection(PalettedWorldState palettedWorldState) {
        this(0, 0, DataPalette.createForChunk(palettedWorldState), DataPalette.createForBiome(palettedWorldState));
    }

    public ChunkSection(ChunkSection original) {
        this(original.blockCount, original.fluidCount, new DataPalette(original.chunkData), new DataPalette(original.biomeData));
    }

    public int getBlock(int x, int y, int z) {
        return this.chunkData.get(x, y, z);
    }

    public void setBlock(int x, int y, int z, int state) {
        int curr = this.chunkData.set(x, y, z, state);
        if (state != AIR && curr == AIR) {
            if (chunkData.getPalettedWorldState().getFluidStates().contains(state)) {
                this.fluidCount++;
            } else {
                this.blockCount++;
            }
        } else if (state == AIR && curr != AIR) {
            if (chunkData.getPalettedWorldState().getFluidStates().contains(curr)) {
                this.fluidCount--;
            } else {
                this.blockCount--;
            }
        }
    }

    public boolean isBlockCountEmpty() {
        return this.blockCount == 0;
    }

    public boolean isFluidCountEmpty() {
        return this.fluidCount == 0;
    }
}
