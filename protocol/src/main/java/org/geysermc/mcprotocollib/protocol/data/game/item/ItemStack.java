package org.geysermc.mcprotocollib.protocol.data.game.item;

import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

@Data
public class ItemStack {
    private final int id;
    private int amount;
    private final @Nullable DataComponents dataComponents;

    public ItemStack(int id, int amount, @Nullable DataComponents dataComponents) {
        this.id = id;
        this.amount = amount;
        this.dataComponents = dataComponents;
    }

    public ItemStack(int id) {
        this(id, 1);
    }

    public ItemStack(int id, int amount) {
        this(id, amount, null);
    }
}
