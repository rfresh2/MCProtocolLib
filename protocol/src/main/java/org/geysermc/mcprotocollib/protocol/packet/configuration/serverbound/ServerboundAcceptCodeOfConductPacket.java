package org.geysermc.mcprotocollib.protocol.packet.configuration.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@NoArgsConstructor
public class ServerboundAcceptCodeOfConductPacket implements MinecraftPacket {
    public static final ServerboundAcceptCodeOfConductPacket INSTANCE = new ServerboundAcceptCodeOfConductPacket();

    @Override
    public void serialize(ByteBuf buf) {}
}
