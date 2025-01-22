package org.geysermc.mcprotocollib.protocol.packet.login.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundLoginFinishedPacket implements MinecraftPacket {
    private final @NonNull GameProfile profile;

    public ClientboundLoginFinishedPacket(ByteBuf in) {
        GameProfile profile = new GameProfile(MinecraftTypes.readUUID(in), MinecraftTypes.readString(in));
        int properties = MinecraftTypes.readVarInt(in);
        List<GameProfile.Property> propertyList = new ArrayList<>(properties);
        for (int index = 0; index < properties; index++) {
            propertyList.add(MinecraftTypes.readProperty(in));
        }
        profile.setProperties(propertyList);
        this.profile = profile;
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeUUID(out, this.profile.getId());
        MinecraftTypes.writeString(out, this.profile.getName());
        MinecraftTypes.writeVarInt(out, this.profile.getProperties().size());
        for (GameProfile.Property property : this.profile.getProperties()) {
            MinecraftTypes.writeProperty(out, property);
        }
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
