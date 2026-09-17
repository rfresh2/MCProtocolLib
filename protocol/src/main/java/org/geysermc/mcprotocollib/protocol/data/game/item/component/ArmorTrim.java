package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.Holder;

import java.util.Map;

@Builder(toBuilder = true)
public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern) {

    @Builder(toBuilder = true)
    public record TrimMaterial(String paletteId, Component description) {
    }

    @Builder(toBuilder = true)
    public record TrimPattern(String assetId, Component description, boolean decal) {
    }
}
