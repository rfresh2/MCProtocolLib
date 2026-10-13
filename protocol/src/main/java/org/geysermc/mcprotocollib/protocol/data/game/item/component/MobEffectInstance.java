package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

@Data
@Builder(toBuilder = true)
public class MobEffectInstance {
    private final Effect effect;
    private final MobEffectDetails details;
}
