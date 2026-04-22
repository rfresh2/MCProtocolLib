package org.geysermc.mcprotocollib.network.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.handshake.serverbound.ClientIntentionPacket;

import java.util.List;
import java.util.zip.DataFormatException;

import static io.netty.util.ByteProcessor.FIND_NON_NUL;

/**
 * Incoming packet size decoder
 */
public class TcpPacketSizeDecoder extends ByteToMessageDecoder {
    public static final String ID = "size-decoder";
    private final Session session;

    public TcpPacketSizeDecoder(final Session session) {
        this.session = session;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        int wlen = in.readableBytes();
        // skip any runs of 0x00 we might find
        int packetStart = in.forEachByte(FIND_NON_NUL);
        if (packetStart == -1) {
            in.clear();
            if (session instanceof TcpServerSession && wlen > 16) {
                throw new CorruptedFrameException("Invalid packet preamble");
            }
            return;
        }
        in.readerIndex(packetStart);

        int readStartIndex = in.readerIndex();
        try {
            int length = readRawVarInt21(in);
            if (packetStart == in.readerIndex()) {
                return;
            }
            if (length < 0) {
                throw new CorruptedFrameException("Packet length too small: " + length);
            }

            if (length > 0) {
                if (session instanceof TcpServerSession serverSession && serverSession.getPacketProtocol().getInboundState() == ProtocolState.HANDSHAKE) {
                    if (validateServerboundHandshakePacket(in, length)) {
                        in.readerIndex(packetStart);
                        return;
                    }
                }
            }

            // note that zero-length packets are ignored
            if (length > 0) {
                if (in.readableBytes() < length) {
                    in.readerIndex(readStartIndex);
                } else {
                    out.add(in.readRetainedSlice(length));
                }
            }
        } catch (Exception e) {
            in.readerIndex(readStartIndex);
            if (e instanceof CorruptedFrameException) {
                throw e;
            }
            throw new CorruptedFrameException(e);
        }
    }

    private boolean validateServerboundHandshakePacket(ByteBuf in, int length) throws Exception {
        final int index = in.readerIndex();
        final int packetId = readRawVarInt21(in);
        // Index hasn't changed, we've read nothing
        if (index == in.readerIndex()) {
            return true;
        }
        final int payloadLength = length - session.getPacketProtocol().getPacketHeader().getLengthSize(packetId);

        // We handle every packet in this phase, if you said something we don't know, something is really wrong
        Class<? extends Packet> packetClass;
        try {
            packetClass = session.getPacketProtocol().getInboundPacketRegistry().getServerboundClass(packetId);
        } catch (IllegalArgumentException e) {
            throw new CorruptedFrameException("Unknown packet", e);
        }
        if (packetClass != ClientIntentionPacket.class) {
            throw new CorruptedFrameException("Received non-handshake packet type: %s".formatted(packetClass.getSimpleName()));
        }

        // We 'technically' have the incoming bytes of a payload here, and so, these can actually parse
        // the packet if needed, so, we'll take advantage of the existing methods
        int expectedMinLen = 7;
        int expectedMaxLen = 2000;
        if (payloadLength > expectedMaxLen) {
            throw new CorruptedFrameException("Handshake packet too large: expected %s buf size %s".formatted(expectedMaxLen, payloadLength));
        }
        if (payloadLength < expectedMinLen) {
            throw new CorruptedFrameException("Handshake packet too small: %s buf size %s".formatted(expectedMinLen, payloadLength));
        }

        in.readerIndex(index);
        return false;
    }

    /**
     * Reads a VarInt from the buffer of up to 21 bits in size.
     *
     * @param buffer the buffer to read from
     * @return the VarInt decoded, {@code 0} if no varint could be read
     */
    private static int readRawVarInt21(ByteBuf buffer) throws DataFormatException {
        if (buffer.readableBytes() < 4) {
            // we don't have enough that we can read a potentially full varint, so fall back to
            // the slow path.
            return readRawVarintSmallBuf(buffer);
        }
        int wholeOrMore = buffer.getIntLE(buffer.readerIndex());

        // take the last three bytes and check if any of them have the high bit set
        int atStop = ~wholeOrMore & 0x808080;
        if (atStop == 0) {
            // all bytes have the high bit set, so the varint we are trying to decode is too wide
            throw new DataFormatException("VarInt too big");
        }

        int bitsToKeep = Integer.numberOfTrailingZeros(atStop) + 1;
        buffer.skipBytes(bitsToKeep >> 3);

        // remove all bits we don't need to keep, a trick from
        // https://github.com/netty/netty/pull/14050#issuecomment-2107750734:
        //
        // > The idea is that thisVarintMask has 0s above the first one of firstOneOnStop, and 1s at
        // > and below it. For example if firstOneOnStop is 0x800080 (where the last 0x80 is the only
        // > one that matters), then thisVarintMask is 0xFF.
        //
        // this is also documented in Hacker's Delight, section 2-1 "Manipulating Rightmost Bits"
        int preservedBytes = wholeOrMore & (atStop ^ (atStop - 1));

        // merge together using this trick: https://github.com/netty/netty/pull/14050#discussion_r1597896639
        preservedBytes = (preservedBytes & 0x007F007F) | ((preservedBytes & 0x00007F00) >> 1);
        preservedBytes = (preservedBytes & 0x00003FFF) | ((preservedBytes & 0x3FFF0000) >> 2);
        return preservedBytes;
    }

    private static int readRawVarintSmallBuf(ByteBuf buffer) {
        if (!buffer.isReadable()) {
            return 0;
        }
        buffer.markReaderIndex();

        byte tmp = buffer.readByte();
        if (tmp >= 0) {
            return tmp;
        }
        int result = tmp & 0x7F;
        if (!buffer.isReadable()) {
            buffer.resetReaderIndex();
            return 0;
        }
        if ((tmp = buffer.readByte()) >= 0) {
            return result | tmp << 7;
        }
        result |= (tmp & 0x7F) << 7;
        if (!buffer.isReadable()) {
            buffer.resetReaderIndex();
            return 0;
        }
        if ((tmp = buffer.readByte()) >= 0) {
            return result | tmp << 14;
        }
        return result | (tmp & 0x7F) << 14;
    }
}
