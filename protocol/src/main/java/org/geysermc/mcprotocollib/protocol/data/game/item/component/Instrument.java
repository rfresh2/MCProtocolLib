package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;

@Data
@Builder(toBuilder = true)
public class Instrument {
    private final Sound soundEvent;
    private final int useDuration;
    private final float range;
}
