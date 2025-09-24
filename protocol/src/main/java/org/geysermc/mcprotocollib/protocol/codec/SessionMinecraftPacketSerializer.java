package org.geysermc.mcprotocollib.protocol.codec;

import io.netty.buffer.ByteBuf;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.codec.PacketDefinition;
import org.geysermc.mcprotocollib.network.codec.SessionPacketSerializer;

public class SessionMinecraftPacketSerializer<T extends MinecraftPacket> extends MinecraftPacketSerializer<T> implements SessionPacketSerializer<T> {
    private final SessionPacketFactory<T> factory;
    public SessionMinecraftPacketSerializer(final SessionPacketFactory<T> factory) {
        super(factory);
        this.factory = factory;
    }

    @Override
    public T deserialize(final ByteBuf buf, final PacketDefinition<T> definition, final Session session) {
        return this.factory.construct(buf, session);
    }
}
