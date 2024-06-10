package org.geysermc.mcprotocollib.protocol.data.game.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.RecipeData;

@Data
@AllArgsConstructor
public class Recipe {
    private final @NonNull RecipeType type;
    private final @NonNull Key identifier;
    private final RecipeData data;
}
