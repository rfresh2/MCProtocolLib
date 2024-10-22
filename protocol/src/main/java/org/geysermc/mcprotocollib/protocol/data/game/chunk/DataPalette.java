package org.geysermc.mcprotocollib.protocol.data.game.chunk;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.ListPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.MapPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.Palette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.SingletonPalette;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DataPalette {

    public static int GLOBAL_PALETTE_BITS_PER_ENTRY = 15;

    private @NonNull Palette palette;
    private BitStorage storage;
    private final PaletteType paletteType;

    public DataPalette(DataPalette original) {
        this(original.palette.copy(), original.storage == null ? null : new BitStorage(original.storage), original.paletteType);
    }

    public static DataPalette createForChunk() {
        return createEmpty(PaletteType.CHUNK);
    }

    public static DataPalette createForBiome() {
        return createEmpty(PaletteType.BIOME);
    }

    public static DataPalette createEmpty(PaletteType paletteType) {
        return new DataPalette(
            createEmptyPalette(paletteType.getMinBitsPerEntry()),
            new BitStorage(paletteType.getMinBitsPerEntry(), paletteType.getStorageSize()),
            paletteType);
    }

    public int get(int x, int y, int z) {
        if (storage != null) {
            int id = this.storage.get(index(x, y, z));
            return this.palette.idToState(id);
        } else {
            return this.palette.idToState(0);
        }
    }

    /**
     * @return the old value present in the storage.
     */
    public int set(int x, int y, int z, int state) {
        int id = this.palette.stateToId(state);
        if (id == -1) {
            resize();
            id = this.palette.stateToId(state);
        }

        if (this.storage != null) {
            int index = index(x, y, z);
            int curr = this.storage.get(index);

            this.storage.set(index, id);
            return curr;
        } else {
            // Singleton palette and the block has not changed because the palette hasn't resized
            return state;
        }
    }

    private int sanitizeBitsPerEntry(int bitsPerEntry) {
        if (bitsPerEntry <= this.paletteType.getMaxBitsPerEntry()) {
            return Math.max(this.paletteType.getMinBitsPerEntry(), bitsPerEntry);
        } else {
            return GLOBAL_PALETTE_BITS_PER_ENTRY;
        }
    }

    private void resize() {
        Palette oldPalette = this.palette;
        BitStorage oldData = this.storage;

        int bitsPerEntry = sanitizeBitsPerEntry(oldPalette instanceof SingletonPalette ? 1 : oldData.getBitsPerEntry() + 1);
        this.palette = createEmptyPalette(bitsPerEntry);
        this.storage = new BitStorage(bitsPerEntry, paletteType.getStorageSize());

        if (oldPalette instanceof SingletonPalette) {
            this.palette.stateToId(oldPalette.idToState(0));
        } else {
            for (int i = 0; i < paletteType.getStorageSize(); i++) {
                this.storage.set(i, this.palette.stateToId(oldPalette.idToState(oldData.get(i))));
            }
        }
    }

    private static Palette createEmptyPalette(int bitsPerEntry) {
        return switch (bitsPerEntry) {
            case 0 -> new SingletonPalette(0);
            case 1,2,3 -> new ListPalette(bitsPerEntry);
            case 4,5,6,7,8 -> new MapPalette(bitsPerEntry);
            default -> GlobalPalette.INSTANCE;
        };
    }

    private int index(int x, int y, int z) {
        return y << paletteType.getMaxBitsPerEntry() | z << paletteType.getMinBitsPerEntry() | x;
    }
}
