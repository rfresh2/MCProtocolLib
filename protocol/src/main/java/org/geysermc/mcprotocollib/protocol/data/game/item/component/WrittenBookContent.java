package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class WrittenBookContent {
    private final Filterable<String> title;
    private final String author;
    private final int generation;
    private final List<Filterable<Component>> pages;
    private final boolean resolved;
}
