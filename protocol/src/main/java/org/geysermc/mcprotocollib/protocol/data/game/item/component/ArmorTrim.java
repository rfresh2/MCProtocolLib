package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.Holder;

import java.util.Map;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern, boolean showInTooltip) {
    public record TrimMaterial(String assetName, int ingredientId, float itemModelIndex,
                               Map<Key, String> overrideArmorMaterials, Component description) {
    }

    public record TrimPattern(String assetId, int templateItemId, Component description, boolean decal) {
    }
}
