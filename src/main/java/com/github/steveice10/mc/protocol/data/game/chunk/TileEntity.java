package com.github.steveice10.mc.protocol.data.game.chunk;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TileEntity {
    private final Position position;
    private CompoundTag compoundTag;
}
