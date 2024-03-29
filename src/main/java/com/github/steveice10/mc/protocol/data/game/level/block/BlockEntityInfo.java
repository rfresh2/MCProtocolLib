package com.github.steveice10.mc.protocol.data.game.level.block;

import com.github.steveice10.opennbt.mini.MNBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@ToString(exclude = "nbt")
public class BlockEntityInfo {
    private int x;
    private int y;
    private int z;
    private BlockEntityType type;
    private @Nullable MNBT nbt;
}
