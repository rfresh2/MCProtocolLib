package org.geysermc.mcprotocollib.protocol.data.game.entity.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.GlobalPos;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor
public class PlayerSpawnInfo {
    private final int dimension;
    private final @NonNull Key worldName;
    private final long hashedSeed;
    private final @NonNull GameMode gameMode;
    private final @Nullable GameMode previousGamemode;
    private final boolean debug;
    private final boolean flat;
    private final @Nullable GlobalPos lastDeathPos;
    private final int portalCooldown;
    private final int seaLevel;
}
