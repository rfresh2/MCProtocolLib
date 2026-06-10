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
        if (in.readBoolean()) {
            this.entityId = OptionalInt.of(MinecraftTypes.readVarInt(in));
        } else {
            this.entityId = OptionalInt.empty();
        }
    }

    @Override
    public void serialize(ByteBuf out) {
        if (this.entityId.isPresent()) {
            out.writeBoolean(true);
            MinecraftTypes.writeVarInt(out, this.entityId.getAsInt());
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public boolean shouldRunOnGameThread() {
        return true;
    }
}
