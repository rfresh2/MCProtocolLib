package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
public class ServerboundBlockEntityTagQueryPacket implements MinecraftPacket {
    private final int transactionId;
    private final int x;
    private final int y;
    private final int z;

    public ServerboundBlockEntityTagQueryPacket(ByteBuf in) {
        this.transactionId = MinecraftTypes.readVarInt(in);
        var position = in.readLong();
        this.x = MinecraftTypes.decodePositionX(position);
        this.y = MinecraftTypes.decodePositionY(position);
        this.z = MinecraftTypes.decodePositionZ(position);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.transactionId);
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
    }
}
