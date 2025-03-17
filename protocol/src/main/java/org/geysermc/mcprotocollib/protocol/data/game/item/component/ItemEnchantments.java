package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ItemEnchantments {
    private final Int2IntMap enchantments;
    private final boolean showInTooltip;
}
