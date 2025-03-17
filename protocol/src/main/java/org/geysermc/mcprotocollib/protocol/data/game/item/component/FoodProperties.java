package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class FoodProperties {
    private final int nutrition;
    private final float saturationModifier;
    private final boolean canAlwaysEat;
}
