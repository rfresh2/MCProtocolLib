package org.geysermc.mcprotocollib.protocol.data.game.chat.numbers;

import com.viaversion.nbt.mini.MNBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.kyori.adventure.text.format.Style;

@Data
@AllArgsConstructor
public class StyledFormat implements NumberFormat {

    /**
     * Serialized {@link Style}
     */
    private final @NonNull MNBT style;
}
