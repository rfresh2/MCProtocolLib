package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import java.util.List;

public record TooltipDisplay(boolean hideTooltip, List<DataComponentType<?>> hiddenComponents) {
}
