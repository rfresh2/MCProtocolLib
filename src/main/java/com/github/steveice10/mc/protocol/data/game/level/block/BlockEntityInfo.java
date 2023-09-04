package com.github.steveice10.mc.protocol.data.game.level.block;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class BlockEntityInfo {
    private int x;
    private int y;
    private int z;
    private BlockEntityType type;
    private @Nullable CompoundTag nbt;
}
