package org.geysermc.mcprotocollib.protocol.packet.configuration.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@NoArgsConstructor
public class ServerboundFinishConfigurationPacket implements MinecraftPacket {
    public static final ServerboundFinishConfigurationPacket INSTANCE = new ServerboundFinishConfigurationPacket();

    public void serialize(ByteBuf out) {
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
