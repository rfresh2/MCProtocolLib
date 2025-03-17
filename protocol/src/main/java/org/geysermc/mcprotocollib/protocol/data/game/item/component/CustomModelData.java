package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record CustomModelData(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
}
