package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata;

import net.kyori.adventure.key.Key;
import lombok.Data;

@Data
public class GlobalPos {
    private final Key dimension;
    private final int x;
    private final int y;
    private final int z;
}
