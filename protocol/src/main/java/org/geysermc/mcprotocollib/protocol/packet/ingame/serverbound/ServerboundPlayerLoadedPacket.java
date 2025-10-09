package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@NoArgsConstructor
public class ServerboundPlayerLoadedPacket implements MinecraftPacket {
    public static final ServerboundPlayerLoadedPacket INSTANCE = new ServerboundPlayerLoadedPacket();

    @Override
    public void serialize(ByteBuf out) {
    }
}
