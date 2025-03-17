package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;

@Data
@Builder(toBuilder = true)
public class Instrument {
    private final Sound soundEvent;
    private final float useDuration;
    private final float range;
    private final Component description;
}
