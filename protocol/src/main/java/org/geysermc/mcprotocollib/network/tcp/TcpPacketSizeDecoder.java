package org.geysermc.mcprotocollib.network.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.codec.VarintByteDecoder;

import java.util.List;
import java.util.zip.DataFormatException;

import static org.geysermc.mcprotocollib.network.codec.VarintByteDecoder.DecodeResult;

/**
 * Incoming packet size decoder
 */
public class TcpPacketSizeDecoder extends ByteToMessageDecoder {
    private final Session session;

    public TcpPacketSizeDecoder(Session session) {
        this.session = session;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        try {
            final VarintByteDecoder reader = new VarintByteDecoder();

            int varintEnd = in.forEachByte(reader);
            if (varintEnd == -1) {
                // We tried to go beyond the end of the buffer. This is probably a good sign that the
                // buffer was too short to hold a proper varint.
                if (reader.getResult() == DecodeResult.RUN_OF_ZEROES) {
                    // Special case where the entire packet is just a run of zeroes. We ignore them all.
                    in.clear();
                }
                return;
            }

            if (reader.getResult() == DecodeResult.RUN_OF_ZEROES) {
                // this will return to the point where the next varint starts
                in.readerIndex(varintEnd);
            } else if (reader.getResult() == DecodeResult.SUCCESS) {
                int readVarint = reader.getReadVarint();
                int bytesRead = reader.getBytesRead();
                if (readVarint < 0) {
                    in.clear();
                    throw new DataFormatException("Bad packet length. Size: " + reader.getReadVarint() + " bytes read: " + reader.getBytesRead());
                } else if (readVarint == 0) {
                    // skip over the empty packet(s) and ignore it
                    in.readerIndex(varintEnd + 1);
                } else {
                    int minimumRead = bytesRead + readVarint;
                    if (in.isReadable(minimumRead)) {
                        out.add(in.retainedSlice(varintEnd + 1, readVarint));
                        in.skipBytes(minimumRead);
                    }
                }
            } else if (reader.getResult() == DecodeResult.TOO_BIG) {
                in.clear();
                throw new DataFormatException("VarInt too big. Size: " + reader.getReadVarint() + " bytes read: " + reader.getBytesRead());
            }
        } catch (final Throwable e) {
            throw new CorruptedFrameException(e);
        }
    }
}
