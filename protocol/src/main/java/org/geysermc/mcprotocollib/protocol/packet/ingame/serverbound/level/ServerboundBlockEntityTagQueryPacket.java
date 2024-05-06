package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ServerboundBlockEntityTagQueryPacket implements MinecraftPacket {
    private final int transactionId;
    private final int x;
    private final int y;
    private final int z;

    public ServerboundBlockEntityTagQueryPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.transactionId = helper.readVarInt(in);
        var position = in.readLong();
        this.x = helper.decodePositionX(position);
        this.y = helper.decodePositionY(position);
        this.z = helper.decodePositionZ(position);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.transactionId);
        helper.writePosition(out, this.x, this.y, this.z);
    }
}
