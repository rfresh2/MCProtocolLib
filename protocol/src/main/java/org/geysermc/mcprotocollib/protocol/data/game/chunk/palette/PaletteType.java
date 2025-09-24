package org.geysermc.mcprotocollib.protocol.data.game.chunk.palette;

import lombok.Getter;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.PalettedWorldState;

@Getter
public enum PaletteType {
    BIOME(1, 3, 64),
    BLOCK_STATE(4, 8, 4096);

    private final int minBitsPerEntry;
    private final int maxBitsPerEntry;
    private final int storageSize;

    PaletteType(int minBitsPerEntry, int maxBitsPerEntry, int storageSize) {
        this.minBitsPerEntry = minBitsPerEntry;
        this.maxBitsPerEntry = maxBitsPerEntry;
        this.storageSize = storageSize;
    }

    public int bitsPerEntry(PalettedWorldState palettedWorldState) {
        return switch (this) {
            case BLOCK_STATE -> palettedWorldState.getBlockStatePaletteBitsPerEntry();
            case BIOME -> palettedWorldState.getBiomePaletteBitsPerEntry();
        };
    }

    public int defaultStateId(PalettedWorldState palettedWorldState) {
        return switch (this) {
            case BLOCK_STATE -> palettedWorldState.getDefaultBlockStateId();
            case BIOME -> palettedWorldState.getDefaultBiomeStateId();
        };
    }
}
