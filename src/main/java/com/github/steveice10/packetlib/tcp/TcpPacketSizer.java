package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.codec.VarintByteDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;

import java.util.List;

public class TcpPacketSizer extends ByteToMessageCodec<ByteBuf> {
    private final Session session;

    private static final DecoderException BAD_LENGTH_CACHED =
        new CorruptedFrameException("Bad packet length");
    private static final DecoderException VARINT_BIG_CACHED =
        new CorruptedFrameException("VarInt too big");

    public TcpPacketSizer(Session session) {
        this.session = session;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        session.getCodecHelper().writeVarInt(out, in.readableBytes());
        out.writeBytes(in);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        final VarintByteDecoder reader = new VarintByteDecoder();

        int varintEnd = in.forEachByte(reader);
        if (varintEnd == -1) {
            // We tried to go beyond the end of the buffer. This is probably a good sign that the
            // buffer was too short to hold a proper varint.
            if (reader.getResult() == VarintByteDecoder.DecodeResult.RUN_OF_ZEROES) {
                // Special case where the entire packet is just a run of zeroes. We ignore them all.
                in.clear();
            }
            return;
        }

        if (reader.getResult() == VarintByteDecoder.DecodeResult.RUN_OF_ZEROES) {
            // this will return to the point where the next varint starts
            in.readerIndex(varintEnd);
        } else if (reader.getResult() == VarintByteDecoder.DecodeResult.SUCCESS) {
            int readVarint = reader.getReadVarint();
            int bytesRead = reader.getBytesRead();
            if (readVarint < 0) {
                in.clear();
                throw BAD_LENGTH_CACHED;
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
        } else if (reader.getResult() == VarintByteDecoder.DecodeResult.TOO_BIG) {
            in.clear();
            throw VARINT_BIG_CACHED;
        }
    }

}
