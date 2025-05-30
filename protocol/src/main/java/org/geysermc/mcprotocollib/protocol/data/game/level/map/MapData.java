package org.geysermc.mcprotocollib.protocol.data.game.level.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString(exclude = "data")
public class MapData {
    private final int columns;
    private final int rows;
    private final int x;
    private final int y;
    private final byte @NonNull [] data;
}
