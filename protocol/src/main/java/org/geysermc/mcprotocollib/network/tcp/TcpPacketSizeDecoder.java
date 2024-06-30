package org.geysermc.mcprotocollib.network.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;
import java.util.zip.DataFormatException;

/**
 * Incoming packet size decoder
 */
public class TcpPacketSizeDecoder extends ByteToMessageDecoder {
    public static String ID = "size-decoder";

    public TcpPacketSizeDecoder() { }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        try {
            int readVarint = 0;
            int bytesRead = 0;
            int varintEnd = 0;
            DecodeResult result = DecodeResult.TOO_SHORT;
            for (int i = in.readerIndex(); i < in.writerIndex(); ++i) {
                byte b = in.getByte(i);
                if (b == 0 && bytesRead == 0) {
                    // tentatively say it's invalid, but there's a possibility of redemption
                    result = DecodeResult.RUN_OF_ZEROES;
                    continue;
                }
                if (result == DecodeResult.RUN_OF_ZEROES) {
                    varintEnd = i;
                    break;
                }
                readVarint |= (b & 0x7F) << bytesRead++ * 7;
                if (bytesRead > 3) {
                    result = DecodeResult.TOO_BIG;
                    varintEnd = i;
                    break;
                }
                if ((b & 0x80) != 128) {
                    result = DecodeResult.SUCCESS;
                    varintEnd = i;
                    break;
                }
                if (i == in.writerIndex() - 1) varintEnd = -1;
            }
            if (varintEnd == -1) {
                // We tried to go beyond the end of the buffer. This is probably a good sign that the
                // buffer was too short to hold a proper varint.
                if (result == DecodeResult.RUN_OF_ZEROES) {
                    // Special case where the entire packet is just a run of zeroes. We ignore them all.
                    in.clear();
                }
                return;
            }

            if (result == DecodeResult.RUN_OF_ZEROES) {
                // this will return to the point where the next varint starts
                in.readerIndex(varintEnd);
            } else if (result == DecodeResult.SUCCESS) {
                if (readVarint < 0) {
                    in.clear();
                    throw new DataFormatException("Bad packet length. Size: " + readVarint + " bytes read: " + bytesRead);
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
            } else if (result == DecodeResult.TOO_BIG) {
                in.clear();
                throw new DataFormatException("VarInt too big. Size: " + readVarint + " bytes read: " + bytesRead);
            }
        } catch (final Throwable e) {
            throw new CorruptedFrameException(e);
        }
    }

    public enum DecodeResult {
        SUCCESS,
        TOO_SHORT,
        TOO_BIG,
        RUN_OF_ZEROES
    }
}
