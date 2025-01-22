package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundPlayerInfoUpdatePacket implements MinecraftPacket {
    private final EnumSet<PlayerListEntryAction> actions;
    private final PlayerListEntry[] entries;

    public ClientboundPlayerInfoUpdatePacket(ByteBuf in) {
        this.actions = MinecraftTypes.readEnumSet(in, PlayerListEntryAction.VALUES, PlayerListEntryAction.class);
        this.entries = new PlayerListEntry[MinecraftTypes.readVarInt(in)];
        for (int count = 0; count < this.entries.length; count++) {
            PlayerListEntry entry = new PlayerListEntry(MinecraftTypes.readUUID(in));
            for (PlayerListEntryAction action : this.actions) {
                switch (action) {
                    case ADD_PLAYER -> {
                        GameProfile profile = new GameProfile(entry.getProfileId(), MinecraftTypes.readString(in, 16));
                        int propertyCount = MinecraftTypes.readVarInt(in);
                        List<GameProfile.Property> propertyList = new ArrayList<>(propertyCount);
                        for (int index = 0; index < propertyCount; index++) {
                            propertyList.add(MinecraftTypes.readProperty(in));
                        }

                        profile.setProperties(propertyList);

                        entry.setProfile(profile);
                    }
                    case INITIALIZE_CHAT -> {
                        if (in.readBoolean()) {
                            entry.setSessionId(MinecraftTypes.readUUID(in));
                            entry.setExpiresAt(in.readLong());
                            byte[] keyBytes = MinecraftTypes.readByteArray(in);
                            entry.setKeySignature(MinecraftTypes.readByteArray(in));

                            PublicKey publicKey;
                            try {
                                publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
                            } catch (GeneralSecurityException e) {
                                throw new IllegalStateException("Could not decode public key.", e);
                            }

                            entry.setPublicKey(publicKey);
                        }
                    }
                    case UPDATE_GAME_MODE -> {
                        GameMode gameMode = GameMode.byId(MinecraftTypes.readVarInt(in));

                        entry.setGameMode(gameMode);
                    }
                    case UPDATE_LISTED -> {
                        boolean listed = in.readBoolean();

                        entry.setListed(listed);
                    }
                    case UPDATE_LATENCY -> {
                        int latency = MinecraftTypes.readVarInt(in);

                        entry.setLatency(latency);
                    }
                    case UPDATE_DISPLAY_NAME -> {
                        Component displayName = MinecraftTypes.readNullable(in, MinecraftTypes::readComponent);

                        entry.setDisplayName(displayName);
                    }
                }
            }

            this.entries[count] = entry;
        }
    }

    public void serialize(ByteBuf out) {
        MinecraftTypes.writeEnumSet(out, this.actions, PlayerListEntryAction.VALUES);
        MinecraftTypes.writeVarInt(out, this.entries.length);
        for (PlayerListEntry entry : this.entries) {
            MinecraftTypes.writeUUID(out, entry.getProfileId());
            for (PlayerListEntryAction action : this.actions) {
                switch (action) {
                    case ADD_PLAYER -> {
                        GameProfile profile = entry.getProfile();
                        if (profile == null) {
                            throw new IllegalArgumentException("Cannot ADD " + entry.getProfileId() + " without a profile.");
                        }

                        MinecraftTypes.writeString(out, profile.getName());
                        MinecraftTypes.writeVarInt(out, profile.getProperties().size());
                        for (GameProfile.Property property : profile.getProperties()) {
                            MinecraftTypes.writeProperty(out, property);
                        }
                    }
                    case INITIALIZE_CHAT -> {
                        out.writeBoolean(entry.getPublicKey() != null);
                        if (entry.getPublicKey() != null) {
                            MinecraftTypes.writeUUID(out, entry.getSessionId());
                            out.writeLong(entry.getExpiresAt());
                            MinecraftTypes.writeByteArray(out, entry.getPublicKey().getEncoded());
                            MinecraftTypes.writeByteArray(out, entry.getKeySignature());
                        }
                    }
                    case UPDATE_GAME_MODE -> MinecraftTypes.writeVarInt(out, entry.getGameMode().ordinal());
                    case UPDATE_LISTED -> out.writeBoolean(entry.isListed());
                    case UPDATE_LATENCY -> MinecraftTypes.writeVarInt(out, entry.getLatency());
                    case UPDATE_DISPLAY_NAME -> MinecraftTypes.writeNullable(out, entry.getDisplayName(), MinecraftTypes::writeComponent);
                }
            }
        }
    }
}
