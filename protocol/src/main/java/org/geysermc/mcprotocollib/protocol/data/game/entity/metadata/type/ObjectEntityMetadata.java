package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type;

import lombok.NonNull;
import lombok.Setter;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;

public class ObjectEntityMetadata<T> extends EntityMetadata<T, MetadataType<T>> {
    @Setter
    private T value;

    public ObjectEntityMetadata(int id, @NonNull MetadataType<T> type, T value) {
        super(id, type);
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }
}
