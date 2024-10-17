package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;

public record Equippable(EquipmentSlot slot, Sound equipSound, @Nullable String model, @Nullable String cameraOverlay,
                         @Nullable HolderSet allowedEntities, boolean dispensable, boolean swappable, boolean damageOnHurt) {
}
