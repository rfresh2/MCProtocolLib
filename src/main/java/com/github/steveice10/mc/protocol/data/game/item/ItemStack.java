package com.github.steveice10.mc.protocol.data.game.item;

import com.github.steveice10.mc.protocol.data.game.item.component.DataComponents;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = "dataComponents")
public class ItemStack {
    private final int id;
    private int amount;
    private final @Nullable DataComponents dataComponents;

    public ItemStack(int id) {
        this(id, 1);
    }

    public ItemStack(int id, int amount) {
        this(id, amount, null);
    }
}
