package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.ModifierOperation;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ItemAttributeModifiers {
    private final List<Entry> modifiers;
    private final boolean showInTooltip;

    @Data
    @Builder(toBuilder = true)
    public static class Entry {
        private final int attribute;
        private final AttributeModifier modifier;
        private final EquipmentSlotGroup slot;
    }

    @Data
    @Builder(toBuilder = true)
    public static class AttributeModifier {
        private final String id;
        private final double amount;
        private final ModifierOperation operation;
    }

    public enum EquipmentSlotGroup {
        ANY,
        MAIN_HAND,
        OFF_HAND,
        HAND,
        FEET,
        LEGS,
        CHEST,
        HEAD,
        ARMOR,
        BODY;

        private static final EquipmentSlotGroup[] VALUES = values();

        public static EquipmentSlotGroup from(int id) {
            return VALUES[id];
        }
    }
}
