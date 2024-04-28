package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

@Data
@AllArgsConstructor
public class Equipment {
    private @NonNull EquipmentSlot slot;
    private @Nullable ItemStack item;
}
