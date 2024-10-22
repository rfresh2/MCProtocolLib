package org.geysermc.mcprotocollib.protocol.data.game.chunk;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class BitStorage {
    private static final int[] MAGIC_VALUES = new int[33];
    static {
        for (int bits = 1; bits < MAGIC_VALUES.length; ++bits) {
            MAGIC_VALUES[bits] = (int) ((((1L << 20) - 1L) / (64L / bits)) + 1);
        }
    }

    @Getter
    private final long @NonNull [] data;
    @Getter
    private final int bitsPerEntry;
    @Getter
    private final int size;

    private final int magic;
    private final int mulBits;

    private final long maxValue;
    private final int valuesPerLong;

    public BitStorage(int bitsPerEntry, int size) {
        this(bitsPerEntry, size, null);
    }

    public BitStorage(int bitsPerEntry, int size, long @Nullable [] data) {
        if (bitsPerEntry < 1 || bitsPerEntry > 32) {
            throw new IllegalArgumentException("bitsPerEntry must be between 1 and 32, inclusive.");
        }
        if (size > 4096) {
            throw new IllegalArgumentException("Size cannot be greater than 4096.");
        }
        this.bitsPerEntry = bitsPerEntry;
        this.size = size;

        this.magic = MAGIC_VALUES[bitsPerEntry];
        this.mulBits = (64 / this.bitsPerEntry) * this.bitsPerEntry;

        this.maxValue = (1L << bitsPerEntry) - 1L;
        this.valuesPerLong = (char) (64 / bitsPerEntry);
        int expectedLength = (size + this.valuesPerLong - 1) / this.valuesPerLong;
        if (data != null) {
            if (data.length != expectedLength) {
                // Hypixel as of 1.19.0
                data = Arrays.copyOf(data, expectedLength);
            }

            this.data = data;
        } else {
            this.data = new long[expectedLength];
        }
    }

    public BitStorage(BitStorage original) {
        this(Arrays.copyOf(original.data, original.data.length), original.bitsPerEntry, original.size, original.magic, original.mulBits,
                original.maxValue, original.valuesPerLong);
    }

    public int get(int index) {
        // assume index in range
        final int full = this.magic * index; // 20 bits of magic + 12 bits of index = barely int
        final int divQ = full >>> 20;
        final int divR = (full & 0xFFFFF) * this.mulBits >>> 20;

        return (int)(this.data[divQ] >>> divR & this.maxValue);
    }

    public void set(int index, int value) {
        // assume index/value in range
        final int full = this.magic * index; // 20 bits of magic + 12 bits of index = barely int
        final int divQ = full >>> 20;
        final int divR = (full & 0xFFFFF) * this.mulBits >>> 20;

        final long[] dataArray = this.data;

        final long data = dataArray[divQ];
        final long mask = this.maxValue;

        final long write = data & ~(mask << divR) | ((long)value & mask) << divR;

        dataArray[divQ] = write;
    }
}
