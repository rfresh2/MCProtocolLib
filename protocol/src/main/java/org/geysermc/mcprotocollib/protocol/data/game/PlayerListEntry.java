package org.geysermc.mcprotocollib.protocol.data.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.UUID;

@Data
@AllArgsConstructor
@ToString(exclude = {"publicKey", "keySignature"})
public class PlayerListEntry {
    private final @NonNull UUID profileId;
    private @NotNull GameProfile profile;
    private boolean listed;
    private int latency;
    private GameMode gameMode;
    private @Nullable Component displayName;
    private UUID sessionId;
    private long expiresAt;
    private @Nullable PublicKey publicKey;
    private byte @Nullable[] keySignature;


    public PlayerListEntry(UUID profileId) {
        this(profileId, new GameProfile(profileId, null), true, 0, GameMode.SURVIVAL, null, null, 0, null, null);
    }

    public PlayerListEntry(String name, UUID profileId) {
        this(profileId, new GameProfile(profileId, name), true, 0, GameMode.SURVIVAL, null, null, 0, null, null);
    }

    public String getName() {
        return this.profile.getName();
    }
}
