package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata;

import lombok.Data;

@Data
public class GlobalPos {
    private final String dimension;
    private final int x;
    private final int y;
    private final int z;
}
