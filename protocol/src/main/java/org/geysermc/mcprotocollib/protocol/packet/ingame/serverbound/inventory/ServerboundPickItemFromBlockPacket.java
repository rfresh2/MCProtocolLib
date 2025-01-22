package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
public class ServerboundPickItemFromBlockPacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final boolean includeData;

    public ServerboundPickItemFromBlockPacket(ByteBuf in) {
        long pos = in.readLong();
        this.x = MinecraftTypes.decodePositionX(pos);
        this.y = MinecraftTypes.decodePositionY(pos);
        this.z = MinecraftTypes.decodePositionZ(pos);
        this.includeData = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
        out.writeBoolean(this.includeData);
    }
}
