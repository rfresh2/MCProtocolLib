package com.github.steveice10.mc.protocol.data.game.entity.metadata.type;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ByteMetadataType;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;

import java.io.UncheckedIOException;

public class ByteEntityMetadata extends EntityMetadata<Byte, ByteMetadataType> {
    private byte value;

    public ByteEntityMetadata(int id, @NonNull ByteMetadataType type, byte value) {
        super(id, type);
        this.value = value;
    }

    public byte getPrimitiveValue() {
        return this.value;
    }

    @Override
    @Deprecated
    public Byte getValue() {
        return this.value;
    }

    @Override
    public void setValue(final Byte value) {
        this.value = value;
    }

    @Override
    public void write(MinecraftCodecHelper helper, ByteBuf out) throws UncheckedIOException {
        this.type.writeMetadataPrimitive(out, this.value);
    }
}
