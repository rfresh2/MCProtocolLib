package org.geysermc.mcprotocollib.protocol.data.game;

import com.github.steveice10.opennbt.mini.MNBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class RegistryEntry {
    private final String id;
    private final @Nullable MNBT data;
}
