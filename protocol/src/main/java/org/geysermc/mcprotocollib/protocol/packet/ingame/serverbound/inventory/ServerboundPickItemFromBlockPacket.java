package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ServerboundPickItemFromBlockPacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final boolean includeData;

    public ServerboundPickItemFromBlockPacket(ByteBuf in, MinecraftCodecHelper helper) {
        long pos = in.readLong();
        this.x = helper.decodePositionX(pos);
        this.y = helper.decodePositionY(pos);
        this.z = helper.decodePositionZ(pos);
        this.includeData = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writePosition(out, this.x, this.y, this.z);
        out.writeBoolean(this.includeData);
    }
}
