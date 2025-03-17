package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ToolData {
    private final List<Rule> rules;
    private final float defaultMiningSpeed;
    private final int damagePerBlock;

    @Data
    @Builder(toBuilder = true)
    public static class Rule {
        private final @NonNull HolderSet blocks;
        private final @Nullable Float speed;
        private final @Nullable Boolean correctForDrops;
    }
}
