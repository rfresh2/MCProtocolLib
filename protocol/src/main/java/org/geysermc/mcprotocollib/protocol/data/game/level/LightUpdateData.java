package org.geysermc.mcprotocollib.protocol.data.game.level;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class LightUpdateData {
    private final long @NonNull [] skyYMask;
    private final long @NonNull [] blockYMask;
    private final long @NonNull [] emptySkyYMask;
    private final long @NonNull [] emptyBlockYMask;
    private final @NonNull List<byte[]> skyUpdates;
    private final @NonNull List<byte[]> blockUpdates;
}
