package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import org.checkerframework.checker.nullness.qual.Nullable;

public record UseCooldown(float seconds, @Nullable String cooldownGroup) {
}
