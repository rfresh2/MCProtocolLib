package com.github.steveice10.mc.protocol.data.game.recipe.data;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.game.recipe.CraftingBookCategory;
import com.github.steveice10.mc.protocol.data.game.recipe.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class ShapedRecipeData implements RecipeData {
    private final int width;
    private final int height;
    private final @NonNull String group;
    private final @NonNull CraftingBookCategory category;
    private final @NonNull Ingredient[] ingredients;
    private final ItemStack result;
    private final boolean showNotification;
}
