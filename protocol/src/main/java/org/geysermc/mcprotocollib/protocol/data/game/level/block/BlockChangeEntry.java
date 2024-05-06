package org.geysermc.mcprotocollib.protocol.data.game.level.block;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockChangeEntry {
    private final int x;
    private final int y;
    private final int z;
    private final int block;
}
