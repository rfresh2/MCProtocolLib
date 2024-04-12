package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.Setter;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.IntMetadataType;

public class IntEntityMetadata extends EntityMetadata<Integer, IntMetadataType> {
    @Setter
    private int value;

    public IntEntityMetadata(int id, @NonNull IntMetadataType type, int value) {
        super(id, type);
        this.value = value;
    }

    public int getPrimitiveValue() {
        return this.value;
    }

    @Override
    @Deprecated
    public Integer getValue() {
        return this.value;
    }

    @Override
    public void write(MinecraftCodecHelper helper, ByteBuf out) {
        this.type.writeMetadataPrimitive(helper, out, value);
    }
}
