package org.geysermc.mcprotocollib.protocol.data.game.chunk;

import lombok.Data;
import lombok.Getter;
import lombok.With;

@Data
@With
public final class PalettedWorldState {
    private final int chunkSectionCount;
    private final int blockStateRegistrySize;
    private final int defaultBlockStateId;
    private final int biomeRegistrySize;
    private final int defaultBiomeStateId;
    @Getter(lazy = true) private final int blockStatePaletteBitsPerEntry = log2Ceil(blockStateRegistrySize);
    @Getter(lazy = true) private final int biomePaletteBitsPerEntry = log2Ceil(biomeRegistrySize);

    static int log2Ceil(int num) {
        return (int) Math.ceil(Math.log(num) / Math.log(2));
    }
}
