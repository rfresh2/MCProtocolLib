package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.OptionalInt;

@Data
@With
@AllArgsConstructor
public class ServerboundSpectatorActionPacket implements MinecraftPacket {
    private final OptionalInt entityId;

    public ServerboundSpectatorActionPacket(ByteBuf in) {
        this.entityId = MinecraftTypes.readOptionalVarInt(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeOptionalVarInt(out, this.entityId);
    }
}
