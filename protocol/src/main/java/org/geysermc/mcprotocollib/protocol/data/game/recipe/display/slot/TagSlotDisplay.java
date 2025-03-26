package org.geysermc.mcprotocollib.protocol.data.game.recipe.display.slot;

public record TagSlotDisplay(String tag) implements SlotDisplay {
    @Override
    public RecipeSlotType getType() {
        return RecipeSlotType.TAG;
    }
}
