package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.Setter;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.LongMetadataType;

public class LongEntityMetadata extends EntityMetadata<Long, LongMetadataType> {
    @Setter
    private long value;

    public LongEntityMetadata(int id, @NonNull LongMetadataType type, long value) {
        super(id, type);
        this.value = value;
    }

    public long getPrimitiveValue() {
        return this.value;
    }

    @Override
    @Deprecated
    public Long getValue() {
        return this.value;
    }

    @Override
    public void write(MinecraftCodecHelper helper, ByteBuf out) {
        this.type.writeMetadataPrimitive(helper, out, value);
    }
}
