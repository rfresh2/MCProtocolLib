package org.geysermc.mcprotocollib.protocol.data.game.item;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;

import java.util.HashMap;
import java.util.Objects;

@Data
@AllArgsConstructor
public class ItemStack {
    private final int id;
    private int amount;
    private final @Nullable DataComponents dataComponents;

    private static final DataComponents EMPTY_COMPONENTS = new DataComponents(ImmutableMap.of());

    public ItemStack(int id) {
        this(id, 1);
    }

    public ItemStack(int id, int amount) {
        this(id, amount, null);
    }

    public @NonNull DataComponents getDataComponentsOrEmpty() {
        return Objects.requireNonNullElse(dataComponents, EMPTY_COMPONENTS);
    }

    public ItemStack withAddedComponents(@NonNull DataComponents addedComponents) {
        var components = new DataComponents(new HashMap<>());
        components.getDataComponents().putAll(addedComponents.getDataComponents());
        components.getDataComponents().putAll(getDataComponentsOrEmpty().getDataComponents());
        return new ItemStack(id, amount, components);
    }

    public ItemStack clone() {
        return new ItemStack(id, amount, dataComponents == null ? null : dataComponents.clone());
    }
}
