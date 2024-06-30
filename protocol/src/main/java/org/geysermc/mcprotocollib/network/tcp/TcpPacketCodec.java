package org.geysermc.mcprotocollib.network.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.codec.PacketCodecHelper;
import org.geysermc.mcprotocollib.network.codec.PacketDefinition;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.packet.PacketProtocol;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;

import java.util.List;

public class TcpPacketCodec extends ByteToMessageCodec<Packet> {
    public static String ID = "codec";
    private final Session session;
    private final PacketCodecHelper codecHelper;
    private final PacketIdSupplier packetIdSupplier;
    private final PacketDefinitionSupplier packetDefinitionSupplier;
    private final PacketSupplier packetSupplier;

    public TcpPacketCodec(Session session, boolean client) {
        this.session = session;
        this.codecHelper = session.getCodecHelper();
        PacketProtocol packetProtocol = session.getPacketProtocol();
        this.packetIdSupplier = client ? packetProtocol::getClientboundId : packetProtocol::getServerboundId;
        this.packetDefinitionSupplier = client ? packetProtocol::getClientboundDefinition : packetProtocol::getServerboundDefinition;
        this.packetSupplier = client ? packetProtocol::createClientboundPacket : packetProtocol::createServerboundPacket;
    }

    @FunctionalInterface
    private interface PacketIdSupplier {
        int get(Packet packet);
    }

    @FunctionalInterface
    private interface PacketDefinitionSupplier {
        @SuppressWarnings("rawtypes")
        PacketDefinition get(int id);
    }

    @FunctionalInterface
    private interface PacketSupplier {
        Packet get(int id, ByteBuf buf, PacketCodecHelper codecHelper);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) {
        int initial = buf.writerIndex();

        try {
            final int packetId = packetIdSupplier.get(packet);
            MinecraftConstants.PACKET_HEADER.writePacketId(buf, codecHelper, packetId);

            final PacketDefinition definition = packetDefinitionSupplier.get(packetId);
            definition.getSerializer().serialize(buf, codecHelper, packet);
        } catch (Throwable t) {
            // Reset writer index to make sure incomplete data is not written out.
            buf.writerIndex(initial);

            if (!this.session.callPacketError(t))  {
                throw t;
            }
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        int initial = buf.readerIndex();

        try {
            int id = MinecraftConstants.PACKET_HEADER.readPacketId(buf, codecHelper);
            if (id == -1) {
                buf.readerIndex(initial);
                return;
            }

            Packet packet = packetSupplier.get(id, buf, codecHelper);

            if (buf.readableBytes() > 0) {
                throw new IllegalStateException("Packet \"" + packet.getClass().getSimpleName() + "\" not fully read.");
            }

            out.add(packet);
        } catch (Throwable t) {
            // Advance buffer to end to make sure remaining data in this packet is skipped.
            buf.readerIndex(buf.readerIndex() + buf.readableBytes());
            if (!this.session.callPacketError(t)) {
                throw t;
            }
        }
    }
}
