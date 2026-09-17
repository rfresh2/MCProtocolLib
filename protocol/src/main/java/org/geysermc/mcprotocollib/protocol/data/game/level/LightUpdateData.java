package org.geysermc.mcprotocollib.protocol.data.game.level;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class LightUpdateData {
    private final byte @NonNull [] skyYMask;
    private final byte @NonNull [] blockYMask;
    private final byte @NonNull [] emptySkyYMask;
    private final byte @NonNull [] emptyBlockYMask;
    private final @NonNull List<byte[]> skyUpdates;
    private final @NonNull List<byte[]> blockUpdates;
}
