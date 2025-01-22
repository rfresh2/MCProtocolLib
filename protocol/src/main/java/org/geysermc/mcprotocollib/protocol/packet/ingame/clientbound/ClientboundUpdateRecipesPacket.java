package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.CraftingBookCategory;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.Ingredient;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.Recipe;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.RecipeType;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.CookedRecipeData;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.RecipeData;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.ShapedRecipeData;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.ShapelessRecipeData;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.SimpleCraftingRecipeData;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.SmithingTransformRecipeData;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.SmithingTrimRecipeData;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.data.StoneCuttingRecipeData;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "recipes")
public class ClientboundUpdateRecipesPacket implements MinecraftPacket {
    private final @NonNull Recipe[] recipes;

    public ClientboundUpdateRecipesPacket(ByteBuf in) {
        this.recipes = new Recipe[MinecraftTypes.readVarInt(in)];
        for (int i = 0; i < this.recipes.length; i++) {
            String identifier = MinecraftTypes.readResourceLocationString(in);
            RecipeType type = RecipeType.from(MinecraftTypes.readVarInt(in));
            RecipeData data;
            switch (type) {
                case CRAFTING_SHAPELESS -> {
                    String group = MinecraftTypes.readString(in);
                    CraftingBookCategory category = CraftingBookCategory.from(MinecraftTypes.readVarInt(in));
                    Ingredient[] ingredients = new Ingredient[MinecraftTypes.readVarInt(in)];
                    for (int j = 0; j < ingredients.length; j++) {
                        ingredients[j] = MinecraftTypes.readRecipeIngredient(in);
                    }

                    ItemStack result = MinecraftTypes.readOptionalItemStack(in);

                    data = new ShapelessRecipeData(group, category, ingredients, result);
                }
                case CRAFTING_SHAPED -> {
                    String group = MinecraftTypes.readString(in);
                    CraftingBookCategory category = CraftingBookCategory.from(MinecraftTypes.readVarInt(in));

                    // ShapedRecipePattern in vanilla
                    int width = MinecraftTypes.readVarInt(in);
                    int height = MinecraftTypes.readVarInt(in);
                    Ingredient[] ingredients = new Ingredient[width * height];
                    for (int j = 0; j < ingredients.length; j++) {
                        ingredients[j] = MinecraftTypes.readRecipeIngredient(in);
                    }

                    ItemStack result = MinecraftTypes.readOptionalItemStack(in);
                    boolean showNotification = in.readBoolean();

                    data = new ShapedRecipeData(width, height, group, category, ingredients, result, showNotification);
                }
                case SMELTING, BLASTING, SMOKING, CAMPFIRE_COOKING -> {
                    String group = MinecraftTypes.readString(in);
                    CraftingBookCategory category = CraftingBookCategory.from(MinecraftTypes.readVarInt(in));
                    Ingredient ingredient = MinecraftTypes.readRecipeIngredient(in);
                    ItemStack result = MinecraftTypes.readOptionalItemStack(in);
                    float experience = in.readFloat();
                    int cookingTime = MinecraftTypes.readVarInt(in);

                    data = new CookedRecipeData(group, category, ingredient, result, experience, cookingTime);
                }
                case STONECUTTING -> {
                    String group = MinecraftTypes.readString(in);
                    Ingredient ingredient = MinecraftTypes.readRecipeIngredient(in);
                    ItemStack result = MinecraftTypes.readOptionalItemStack(in);

                    data = new StoneCuttingRecipeData(group, ingredient, result);
                }
                case SMITHING_TRANSFORM -> {
                    Ingredient template = MinecraftTypes.readRecipeIngredient(in);
                    Ingredient base = MinecraftTypes.readRecipeIngredient(in);
                    Ingredient addition = MinecraftTypes.readRecipeIngredient(in);
                    ItemStack result = MinecraftTypes.readOptionalItemStack(in);

                    data = new SmithingTransformRecipeData(template, base, addition, result);
                }
                case SMITHING_TRIM -> {
                    Ingredient template = MinecraftTypes.readRecipeIngredient(in);
                    Ingredient base = MinecraftTypes.readRecipeIngredient(in);
                    Ingredient addition = MinecraftTypes.readRecipeIngredient(in);

                    data = new SmithingTrimRecipeData(template, base, addition);
                }
                default -> {
                    CraftingBookCategory category = CraftingBookCategory.from(MinecraftTypes.readVarInt(in));

                    data = new SimpleCraftingRecipeData(category);
                }
            }

            this.recipes[i] = new Recipe(type, identifier, data);
        }
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.recipes.length);
        for (Recipe recipe : this.recipes) {
            MinecraftTypes.writeResourceLocation(out, recipe.getIdentifier());
            MinecraftTypes.writeVarInt(out, recipe.getType().ordinal());
            switch (recipe.getType()) {
                case CRAFTING_SHAPELESS -> {
                    ShapelessRecipeData data = (ShapelessRecipeData) recipe.getData();

                    MinecraftTypes.writeString(out, data.getGroup());
                    MinecraftTypes.writeVarInt(out, data.getCategory().ordinal());
                    MinecraftTypes.writeVarInt(out, data.getIngredients().length);
                    for (Ingredient ingredient : data.getIngredients()) {
                        MinecraftTypes.writeRecipeIngredient(out, ingredient);
                    }

                    MinecraftTypes.writeOptionalItemStack(out, data.getResult());
                }
                case CRAFTING_SHAPED -> {
                    ShapedRecipeData data = (ShapedRecipeData) recipe.getData();
                    if (data.getIngredients().length != data.getWidth() * data.getHeight()) {
                        throw new IllegalStateException("Shaped recipe must have ingredient count equal to width * height.");
                    }

                    MinecraftTypes.writeString(out, data.getGroup());
                    MinecraftTypes.writeVarInt(out, data.getCategory().ordinal());

                    // ShapedRecipePattern in vanilla
                    MinecraftTypes.writeVarInt(out, data.getWidth());
                    MinecraftTypes.writeVarInt(out, data.getHeight());
                    for (Ingredient ingredient : data.getIngredients()) {
                        MinecraftTypes.writeRecipeIngredient(out, ingredient);
                    }

                    MinecraftTypes.writeOptionalItemStack(out, data.getResult());
                    out.writeBoolean(data.isShowNotification());
                }
                case SMELTING, BLASTING, SMOKING, CAMPFIRE_COOKING -> {
                    CookedRecipeData data = (CookedRecipeData) recipe.getData();

                    MinecraftTypes.writeString(out, data.getGroup());
                    MinecraftTypes.writeVarInt(out, data.getCategory().ordinal());
                    MinecraftTypes.writeRecipeIngredient(out, data.getIngredient());
                    MinecraftTypes.writeOptionalItemStack(out, data.getResult());
                    out.writeFloat(data.getExperience());
                    MinecraftTypes.writeVarInt(out, data.getCookingTime());
                }
                case STONECUTTING -> {
                    StoneCuttingRecipeData data = (StoneCuttingRecipeData) recipe.getData();

                    MinecraftTypes.writeString(out, data.getGroup());
                    MinecraftTypes.writeRecipeIngredient(out, data.getIngredient());
                    MinecraftTypes.writeOptionalItemStack(out, data.getResult());
                }
                case SMITHING_TRANSFORM -> {
                    SmithingTransformRecipeData data = (SmithingTransformRecipeData) recipe.getData();

                    MinecraftTypes.writeRecipeIngredient(out, data.getTemplate());
                    MinecraftTypes.writeRecipeIngredient(out, data.getBase());
                    MinecraftTypes.writeRecipeIngredient(out, data.getAddition());
                    MinecraftTypes.writeOptionalItemStack(out, data.getResult());
                }
                case SMITHING_TRIM -> {
                    SmithingTrimRecipeData data = (SmithingTrimRecipeData) recipe.getData();

                    MinecraftTypes.writeRecipeIngredient(out, data.getTemplate());
                    MinecraftTypes.writeRecipeIngredient(out, data.getBase());
                    MinecraftTypes.writeRecipeIngredient(out, data.getAddition());
                }
                default -> {
                    SimpleCraftingRecipeData data = (SimpleCraftingRecipeData) recipe.getData();

                    MinecraftTypes.writeVarInt(out, data.getCategory().ordinal());
                }
            }
        }
    }
}
