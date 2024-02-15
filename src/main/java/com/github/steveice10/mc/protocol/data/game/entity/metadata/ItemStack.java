package com.github.steveice10.mc.protocol.data.game.entity.metadata;

import com.github.steveice10.opennbt.mini.MNBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class ItemStack {
    private final int id;
    private int amount;
    private final @Nullable MNBT nbt;

    public ItemStack(int id) {
        this(id, 1);
    }

    public ItemStack(int id, int amount) {
        this(id, amount, null);
    }
}
