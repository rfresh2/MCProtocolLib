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
    private final OutboundPacketIdEncoder outboundPacketIdEncoder;
    private final OutboundPacketDefinitionSupplier outboundPacketDefinitionSupplier;
    private final InboundPacketFactory inboundPacketFactory;

    public TcpPacketCodec(Session session, boolean client) {
        this.session = session;
        this.codecHelper = session.getCodecHelper();
        PacketProtocol packetProtocol = session.getPacketProtocol();
        this.outboundPacketIdEncoder = client
            ? (packet) -> packetProtocol.getOutboundPacketRegistry().getServerboundId(packet)
            : (packet) -> packetProtocol.getOutboundPacketRegistry().getClientboundId(packet);
        this.outboundPacketDefinitionSupplier = client
            ? (id) -> packetProtocol.getOutboundPacketRegistry().getServerboundDefinition(id)
            : (id) -> packetProtocol.getOutboundPacketRegistry().getClientboundDefinition(id);
        this.inboundPacketFactory = client
            ? (id, buf, codecHelper) -> packetProtocol.getInboundPacketRegistry().createClientboundPacket(id, buf, codecHelper)
            : (id, buf, codecHelper) -> packetProtocol.getInboundPacketRegistry().createServerboundPacket(id, buf, codecHelper);
    }

    @FunctionalInterface
    private interface OutboundPacketIdEncoder {
        int get(Packet packet);
    }

    @FunctionalInterface
    private interface OutboundPacketDefinitionSupplier {
        @SuppressWarnings("rawtypes")
        PacketDefinition get(int id);
    }

    @FunctionalInterface
    private interface InboundPacketFactory {
        Packet get(int id, ByteBuf buf, PacketCodecHelper codecHelper);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) {
        int initial = buf.writerIndex();

        try {
            final int packetId = outboundPacketIdEncoder.get(packet);
            MinecraftConstants.PACKET_HEADER.writePacketId(buf, codecHelper, packetId);

            final PacketDefinition definition = outboundPacketDefinitionSupplier.get(packetId);
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
        // Vanilla also checks for 0 length
        if (buf.readableBytes() == 0) {
            return;
        }

        int initial = buf.readerIndex();

        try {
            int id = MinecraftConstants.PACKET_HEADER.readPacketId(buf, codecHelper);
            if (id == -1) {
                buf.readerIndex(initial);
                return;
            }

            Packet packet = inboundPacketFactory.get(id, buf, codecHelper);

            if (buf.readableBytes() > 0) {
                throw new IllegalStateException("Packet \"" + packet.getClass().getSimpleName() + "\" not fully read.");
            }

            out.add(packet);
            if (packet != null && packet.isTerminal()) {
                // Next packets are in a different protocol state, so we must
                // disable auto-read to prevent reading wrong packets.
                session.setAutoRead(false);
            }
        } catch (Throwable t) {
            // Advance buffer to end to make sure remaining data in this packet is skipped.
            buf.readerIndex(buf.readerIndex() + buf.readableBytes());
            if (!this.session.callPacketError(t)) {
                throw t;
            }
        }
    }
}
