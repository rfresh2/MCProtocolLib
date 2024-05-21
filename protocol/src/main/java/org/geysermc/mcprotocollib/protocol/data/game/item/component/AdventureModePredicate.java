package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import com.viaversion.nbt.mini.MNBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor
public class AdventureModePredicate {
    private final List<BlockPredicate> predicates;
    private final boolean showInTooltip;

    @Data
    @AllArgsConstructor
    public static class BlockPredicate {
        private final @Nullable HolderSet blocks;
        private final @Nullable List<PropertyMatcher> properties;
        private final @Nullable MNBT nbt;
    }

    @Data
    @AllArgsConstructor
    public static class PropertyMatcher {
        private final String name;
        private final @Nullable String value;
        private final @Nullable String minValue;
        private final @Nullable String maxValue;
    }
}
