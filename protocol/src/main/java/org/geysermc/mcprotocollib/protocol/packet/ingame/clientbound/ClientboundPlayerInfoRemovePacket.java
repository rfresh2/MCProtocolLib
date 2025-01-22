package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@With
@AllArgsConstructor
public class ClientboundPlayerInfoRemovePacket implements MinecraftPacket {
    private final List<UUID> profileIds;

    public ClientboundPlayerInfoRemovePacket(ByteBuf in) {
        int numIds = MinecraftTypes.readVarInt(in);
        this.profileIds = new ArrayList<>(numIds);
        for (int i = 0; i < numIds; i++) {
            this.profileIds.add(MinecraftTypes.readUUID(in));
        }
    }

    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.profileIds.size());
        for (UUID id : this.profileIds) {
            MinecraftTypes.writeUUID(out, id);
        }
    }
}
