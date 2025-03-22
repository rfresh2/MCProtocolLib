package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record TooltipDisplay(boolean hideTooltip, List<DataComponentType<?>> hiddenComponents) {
}
