package com.github.steveice10.mc.protocol.codec;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.codec.PacketCodecHelper;
import com.github.steveice10.packetlib.codec.PacketDefinition;
import com.github.steveice10.packetlib.packet.PacketHeader;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class PacketStateCodec extends PacketProtocol {

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getSRVRecordPrefix() {
        return MinecraftConstants.SRV_RECORD_PREFIX;
    }

    @Override
    public PacketHeader getPacketHeader() {
        return MinecraftConstants.PACKET_HEADER;
    }

    @Override
    public PacketCodecHelper createHelper() {
        throw new UnsupportedOperationException("Not supported!");
    }

    @Override
    public void newClientSession(Session session) {
        throw new UnsupportedOperationException("Not supported!");
    }

    @Override
    public void newServerSession(Server server, Session session) {
        throw new UnsupportedOperationException("Not supported!");
    }

    public static class Builder {
        private final Int2ObjectMap<PacketDefinition<? extends MinecraftPacket, MinecraftCodecHelper>> clientboundPackets = new Int2ObjectOpenHashMap<>();
        private final Int2ObjectMap<PacketDefinition<? extends MinecraftPacket, MinecraftCodecHelper>> serverboundPackets = new Int2ObjectOpenHashMap<>();

        public <T extends MinecraftPacket> Builder registerClientboundPacket(int id, Class<T> packetClass, PacketFactory<T, MinecraftCodecHelper> factory) {
            this.clientboundPackets.put(id, new PacketDefinition<>(id, packetClass, new MinecraftPacketSerializer<>(factory)));
            return this;
        }

        public <T extends MinecraftPacket> Builder registerServerboundPacket(int id, Class<T> packetClass, PacketFactory<T, MinecraftCodecHelper> factory) {
            this.serverboundPackets.put(id, new PacketDefinition<>(id, packetClass, new MinecraftPacketSerializer<>(factory)));
            return this;
        }

        public PacketStateCodec build() {
            PacketStateCodec codec = new PacketStateCodec();
            this.clientboundPackets.values().forEach(codec::registerClientbound);
            this.serverboundPackets.values().forEach(codec::registerServerbound);
            return codec;
        }
    }
}
