package org.geysermc.mcprotocollib.protocol.codec;

import io.netty.buffer.ByteBuf;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;

public interface SessionPacketFactory<T extends Packet> extends PacketFactory<T> {
    T construct(ByteBuf buf, Session channel);

    default T construct(ByteBuf buf) {
        throw new UnsupportedOperationException("Session is required to construct this packet");
    }
}
