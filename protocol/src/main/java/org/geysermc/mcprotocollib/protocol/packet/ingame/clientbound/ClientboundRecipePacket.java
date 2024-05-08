package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.UnlockRecipesAction;

import java.util.Arrays;

@Data
@With
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = {"recipeIdsToChange", "recipeIdsToInit"})
public class ClientboundRecipePacket implements MinecraftPacket {
    private final @NonNull UnlockRecipesAction action;

    private final @NonNull String[] recipeIdsToChange;
    private final boolean openCraftingBook;
    private final boolean activateCraftingFiltering;
    private final boolean openSmeltingBook;
    private final boolean activateSmeltingFiltering;
    private final boolean openBlastingBook;
    private final boolean activateBlastingFiltering;
    private final boolean openSmokingBook;
    private final boolean activateSmokingFiltering;

    private final String[] recipeIdsToInit;

    public ClientboundRecipePacket(@NonNull String[] recipeIdsToChange,
                                   boolean openCraftingBook, boolean activateCraftingFiltering,
                                   boolean openSmeltingBook, boolean activateSmeltingFiltering,
                                   boolean openBlastingBook, boolean activateBlastingFiltering,
                                   boolean openSmokingBook, boolean activateSmokingFiltering,
                                   @NonNull UnlockRecipesAction action) {
        if (action != UnlockRecipesAction.ADD && action != UnlockRecipesAction.REMOVE) {
            throw new IllegalArgumentException("Action must be ADD or REMOVE.");
        }

        this.action = action;
        this.recipeIdsToChange = Arrays.copyOf(recipeIdsToChange, recipeIdsToChange.length);
        this.openCraftingBook = openCraftingBook;
        this.activateCraftingFiltering = activateCraftingFiltering;
        this.openSmeltingBook = openSmeltingBook;
        this.activateSmeltingFiltering = activateSmeltingFiltering;
        this.openBlastingBook = openBlastingBook;
        this.activateBlastingFiltering = activateBlastingFiltering;
        this.openSmokingBook = openSmokingBook;
        this.activateSmokingFiltering = activateSmokingFiltering;

        this.recipeIdsToInit = null;
    }

    public ClientboundRecipePacket(@NonNull String[] recipeIdsToChange,
                                   boolean openCraftingBook, boolean activateCraftingFiltering,
                                   boolean openSmeltingBook, boolean activateSmeltingFiltering,
                                   boolean openBlastingBook, boolean activateBlastingFiltering,
                                   boolean openSmokingBook, boolean activateSmokingFiltering,
                                   @NonNull String[] recipeIdsToInit) {
        this.action = UnlockRecipesAction.INIT;
        this.recipeIdsToChange = Arrays.copyOf(recipeIdsToChange, recipeIdsToChange.length);
        this.openCraftingBook = openCraftingBook;
        this.activateCraftingFiltering = activateCraftingFiltering;
        this.openSmeltingBook = openSmeltingBook;
        this.activateSmeltingFiltering = activateSmeltingFiltering;
        this.openBlastingBook = openBlastingBook;
        this.activateBlastingFiltering = activateBlastingFiltering;
        this.openSmokingBook = openSmokingBook;
        this.activateSmokingFiltering = activateSmokingFiltering;

        this.recipeIdsToInit = Arrays.copyOf(recipeIdsToInit, recipeIdsToInit.length);
    }

    public ClientboundRecipePacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.action = UnlockRecipesAction.from(helper.readVarInt(in));

        this.openCraftingBook = in.readBoolean();
        this.activateCraftingFiltering = in.readBoolean();
        this.openSmeltingBook = in.readBoolean();
        this.activateSmeltingFiltering = in.readBoolean();
        this.openBlastingBook = in.readBoolean();
        this.activateBlastingFiltering = in.readBoolean();
        this.openSmokingBook = in.readBoolean();
        this.activateSmokingFiltering = in.readBoolean();

        this.recipeIdsToChange = new String[helper.readVarInt(in)];
        for (int i = 0; i < this.recipeIdsToChange.length; i++) {
            this.recipeIdsToChange[i] = helper.readString(in);
        }
        if (this.action == UnlockRecipesAction.INIT) {
            this.recipeIdsToInit = new String[helper.readVarInt(in)];
            for (int i = 0; i < this.recipeIdsToInit.length; i++) {
                this.recipeIdsToInit[i] = helper.readString(in);
            }
        } else {
            this.recipeIdsToInit = null;
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.action.ordinal());

        out.writeBoolean(this.openCraftingBook);
        out.writeBoolean(this.activateCraftingFiltering);
        out.writeBoolean(this.openSmeltingBook);
        out.writeBoolean(this.activateSmeltingFiltering);
        out.writeBoolean(this.openBlastingBook);
        out.writeBoolean(this.activateBlastingFiltering);
        out.writeBoolean(this.openSmokingBook);
        out.writeBoolean(this.activateSmokingFiltering);

        helper.writeVarInt(out, this.recipeIdsToChange.length);
        for (String recipeId : this.recipeIdsToChange) {
            helper.writeString(out, recipeId);
        }
        if (this.action == UnlockRecipesAction.INIT) {
            helper.writeVarInt(out, this.recipeIdsToInit.length);
            for (String recipeId : this.recipeIdsToInit) {
                helper.writeString(out, recipeId);
            }
        }
    }
}
