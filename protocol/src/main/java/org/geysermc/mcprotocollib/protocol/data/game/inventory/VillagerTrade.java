package org.geysermc.mcprotocollib.protocol.data.game.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.ItemStack;

@Data
@AllArgsConstructor
public class VillagerTrade {
    private final @Nullable ItemStack firstInput;
    private final @Nullable ItemStack secondInput;
    private final @Nullable ItemStack output;
    private final boolean tradeDisabled;
    private final int numUses;
    private final int maxUses;
    private final int xp;
    private final int specialPrice;
    private final float priceMultiplier;
    private final int demand;
}
