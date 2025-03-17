package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Fireworks {
    private final int flightDuration;
    private final List<FireworkExplosion> explosions;

    @Data
    @Builder(toBuilder = true)
    public static class FireworkExplosion {
        private final int shapeId;
        private final int[] colors;
        private final int[] fadeColors;
        private final boolean hasTrail;
        private final boolean hasTwinkle;
    }
}
