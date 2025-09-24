package org.geysermc.mcprotocollib.network.codec;

import io.netty.buffer.ByteBuf;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;

public interface SessionPacketSerializer<T extends Packet> extends PacketSerializer<T> {
    T deserialize(ByteBuf buf, PacketDefinition<T> definition, Session session);
}
