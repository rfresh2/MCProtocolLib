package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DyedItemColor {
    private final int rgb;
    private final boolean showInTooltip;
}
