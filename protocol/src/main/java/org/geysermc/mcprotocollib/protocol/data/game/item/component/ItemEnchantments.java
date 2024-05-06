package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemEnchantments {
    private final Int2IntMap enchantments;
    private final boolean showInTooltip;
}
