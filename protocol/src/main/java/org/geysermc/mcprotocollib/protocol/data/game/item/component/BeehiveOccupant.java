package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import com.viaversion.nbt.mini.MNBT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class BeehiveOccupant {
    private final MNBT entityData;
    private final int ticksInHive;
    private final int minTicksInHive;
}
