package org.geysermc.mcprotocollib.protocol.data.game.chunk.palette;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;

/**
 * A palette containing one state.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class SingletonPalette implements Palette {
    private final int state;

    public SingletonPalette(ByteBuf in, MinecraftCodecHelper helper) {
        this.state = helper.readVarInt(in);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int stateToId(int state) {
        return this.state == state ? 0 : -1;
    }

    @Override
    public int idToState(int id) {
        if (id != 0) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return this.state;
    }

    @Override
    public SingletonPalette copy() {
        return this;
    }
}
