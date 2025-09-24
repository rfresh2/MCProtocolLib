package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import com.viaversion.nbt.mini.MNBT;
import lombok.Builder;

@Builder(toBuilder = true)
public record TypedEntityData<T>(T type, MNBT tag) {
    // TODO: Improve this implementation, too bulky in DataComponentTypes
}
