package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import org.jspecify.annotations.Nullable;

public record UseCooldown(float seconds, @Nullable String cooldownGroup) {
}
