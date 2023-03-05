package com.github.steveice10.mc.protocol.data.game.chunk;

import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import com.github.steveice10.mc.protocol.util.NetUtil;
import com.github.steveice10.mc.protocol.util.ObjectUtil;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Setter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Setter
public class BlockStorage {
    private static final BlockState AIR = new BlockState(0, 0);

    private int bitsPerEntry;

    private List<Integer> states;

    private FlexibleStorage storage;

    public BlockStorage() {
        this.bitsPerEntry = 4;

        this.states = new IntArrayList();
        this.states.add(AIR.toInt());

        this.storage = new FlexibleStorage(this.bitsPerEntry, 4096);
    }

    public BlockStorage(NetInput in) throws IOException {
        this.bitsPerEntry = in.readUnsignedByte();

        this.states = new IntArrayList();
        int stateCount = in.readVarInt();
        for(int i = 0; i < stateCount; i++) {
            this.states.add(NetUtil.readBlockStateInt(in));
        }

        this.storage = new FlexibleStorage(this.bitsPerEntry, in.readLongs(in.readVarInt()));
    }

    private static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    private static BlockState rawToState(int raw) {
        return new BlockState(raw >> 4, raw & 0xF);
    }

    private static int stateToRaw(BlockState state) {
        return (state.getId() << 4) | (state.getData() & 0xF);
    }

    public void write(NetOutput out) throws IOException {
        out.writeByte(this.bitsPerEntry);

        out.writeVarInt(this.states.size());
        for(Integer state : this.states) {
            NetUtil.writeBlockStateInt(out, state);
        }

        long[] data = this.storage.getData();
        out.writeVarInt(data.length);
        out.writeLongs(data);
    }

    public int getBitsPerEntry() {
        return this.bitsPerEntry;
    }

    public List<Integer> getStates() {
        return Collections.unmodifiableList(this.states);
    }

    public FlexibleStorage getStorage() {
        return this.storage;
    }

    public BlockState get(int x, int y, int z) {
        int id = this.storage.get(index(x, y, z));
        return this.bitsPerEntry <= 8 ? (id >= 0 && id < this.states.size() ? rawToState(this.states.get(id)) : AIR) : rawToState(id);
    }

    public void set(int x, int y, int z, BlockState state) {
        int id = this.bitsPerEntry <= 8 ? this.states.indexOf(state.toInt()) : stateToRaw(state);
        if(id == -1) {
            this.states.add(state.toInt());
            if(this.states.size() > 1 << this.bitsPerEntry) {
                this.bitsPerEntry++;

                List<Integer> oldStates = this.states;
                if(this.bitsPerEntry > 8) {
                    oldStates = new IntArrayList(this.states);
                    this.states.clear();
                    this.bitsPerEntry = 13;
                }

                FlexibleStorage oldStorage = this.storage;
                this.storage = new FlexibleStorage(this.bitsPerEntry, this.storage.getSize());
                for(int index = 0; index < this.storage.getSize(); index++) {
                    this.storage.set(index, this.bitsPerEntry <= 8 ? oldStorage.get(index) : oldStates.get(index));
                }
            }

            id = this.bitsPerEntry <= 8 ? this.states.indexOf(state.toInt()) : state.toInt();
        }

        this.storage.set(index(x, y, z), id);
    }

    public boolean isEmpty() {
        for(int index = 0; index < this.storage.getSize(); index++) {
            if(this.storage.get(index) != 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof BlockStorage)) return false;

        BlockStorage that = (BlockStorage) o;
        return this.bitsPerEntry == that.bitsPerEntry &&
                Objects.equals(this.states, that.states) &&
                Objects.equals(this.storage, that.storage);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(this.bitsPerEntry, this.states, this.storage);
    }

    @Override
    public String toString() {
        return ObjectUtil.toString(this);
    }
}
