package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@NoArgsConstructor
public class ServerboundClientTickEndPacket implements MinecraftPacket {
    public static final ServerboundClientTickEndPacket INSTANCE = new ServerboundClientTickEndPacket();

    @Override
    public void serialize(ByteBuf out) {
    }
}
