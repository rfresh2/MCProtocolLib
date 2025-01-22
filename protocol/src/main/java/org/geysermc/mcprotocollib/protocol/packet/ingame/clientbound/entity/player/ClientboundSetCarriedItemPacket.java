package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ClientboundSetCarriedItemPacket implements MinecraftPacket {
    private final int slot;

    public ClientboundSetCarriedItemPacket(ByteBuf in) {
        this.slot = in.readByte();
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeByte(this.slot);
    }
}
