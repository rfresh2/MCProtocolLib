package com.github.steveice10.mc.protocol.data.game.chunk;

import com.github.steveice10.mc.protocol.util.ObjectUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Setter
@Getter
public class FlexibleStorage {
    private long[] data;
    private int bitsPerEntry;
    private int size;
    private long maxEntryValue;

    public FlexibleStorage(int bitsPerEntry, int size) {
        this(bitsPerEntry, new long[roundToNearest(size * bitsPerEntry, 64) / 64]);
    }

    public FlexibleStorage(int bitsPerEntry, long[] data) {
        if(bitsPerEntry < 4) {
            bitsPerEntry = 4;
        }

        this.bitsPerEntry = bitsPerEntry;
        this.data = data;

        this.size = this.data.length * 64 / this.bitsPerEntry;
        this.maxEntryValue = (1L << this.bitsPerEntry) - 1;
    }

    private static int roundToNearest(int value, int roundTo) {
        if(roundTo == 0) {
            return 0;
        } else if(value == 0) {
            return roundTo;
        } else {
            if(value < 0) {
                roundTo *= -1;
            }

            int remainder = value % roundTo;
            return remainder != 0 ? value + roundTo - remainder : value;
        }
    }

    public long[] getData() {
        return this.data;
    }

    public int getBitsPerEntry() {
        return this.bitsPerEntry;
    }

    public int getSize() {
        return this.size;
    }

    public int get(int index) {
        if(index < 0 || index > this.size - 1) {
            throw new IndexOutOfBoundsException();
        }

        int individualValueMask = (1 << this.bitsPerEntry) - 1;
        int startLong = (index * this.bitsPerEntry) / 64;
        int startOffset = (index * this.bitsPerEntry) % 64;
        int endLong = ((index + 1) * this.bitsPerEntry - 1) / 64;

        int d;
        if (startLong == endLong) {
            d = (int) (this.data[startLong] >>> startOffset);
        } else {
            int endOffset = 64 - startOffset;
            d = (int) (this.data[startLong] >>> startOffset | this.data[endLong] << endOffset);
        }
        d &= individualValueMask;
        return d;
    }

    public void set(int index, int value) {
        if(index < 0 || index > this.size - 1) {
            throw new IndexOutOfBoundsException();
        }

        if(value < 0 || value > this.maxEntryValue) {
            throw new IllegalArgumentException("Value cannot be outside of accepted range.");
        }
        int individualValueMask = (1 << this.bitsPerEntry) - 1;
        int startLong = (index * this.bitsPerEntry) / 64;
        int startOffset = (index * this.bitsPerEntry) % 64;
        int endLong = ((index + 1) * this.bitsPerEntry - 1) / 64;
        long d = value & individualValueMask;

        data[startLong] &= ~((long) individualValueMask << startOffset);
        data[startLong] |= (d << startOffset);

        if (startLong != endLong) {
            data[endLong] &= ~(individualValueMask >> (64 - startOffset));
            data[endLong] |= (d >> (64 - startOffset));
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof FlexibleStorage)) return false;

        FlexibleStorage that = (FlexibleStorage) o;
        return Arrays.equals(this.data, that.data) &&
                this.bitsPerEntry == that.bitsPerEntry &&
                this.size == that.size &&
                this.maxEntryValue == that.maxEntryValue;
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(this.data, this.bitsPerEntry, this.size, this.maxEntryValue);
    }

    @Override
    public String toString() {
        return ObjectUtil.toString(this);
    }
}
