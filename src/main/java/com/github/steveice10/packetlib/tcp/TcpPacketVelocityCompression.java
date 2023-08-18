package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.packetlib.Session;
import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TcpPacketVelocityCompression extends ByteToMessageCodec<ByteBuf> {
    private static final int MAX_COMPRESSED_SIZE = 2097152;

    private final Session session;
    private final boolean validateDecompression;
    private final VelocityCompressor velocityCompressor;
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpPacketVelocityCompression.class);
    public TcpPacketVelocityCompression(Session session, boolean validateDecompression) {
        this.session = session;
        this.validateDecompression = validateDecompression;
        this.velocityCompressor = Natives.compress.get().create(4);
        LOGGER.debug("Velocity compression initialized with {} variant.", Natives.compress.getLoadedVariant());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        this.velocityCompressor.close();
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        int readable = in.readableBytes();
        if(readable < this.session.getCompressionThreshold()) {
            session.getCodecHelper().writeVarInt(out, 0);
            out.writeBytes(in);
        } else {
            session.getCodecHelper().writeVarInt(out, readable);
            final ByteBuf byteBufCompat = MoreByteBufUtils.ensureCompatible(ctx.alloc(), this.velocityCompressor, in);
            try {
                this.velocityCompressor.deflate(byteBufCompat, out);
            } finally {
                byteBufCompat.release();
            }
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if(buf.readableBytes() != 0) {
            int size = this.session.getCodecHelper().readVarInt(buf);
            if(size == 0) {
                out.add(buf.readBytes(buf.readableBytes()));
            } else {
                if (validateDecompression) { // This is sectioned off as of at least Java Edition 1.18
                    if (size < this.session.getCompressionThreshold()) {
                        throw new DecoderException("Badly compressed packet: size of " + size + " is below threshold of " + this.session.getCompressionThreshold() + ".");
                    }

                    if (size > MAX_COMPRESSED_SIZE) {
                        throw new DecoderException("Badly compressed packet: size of " + size + " is larger than protocol maximum of " + MAX_COMPRESSED_SIZE + ".");
                    }
                }

                final ByteBuf compatibleIn = MoreByteBufUtils.ensureCompatible(ctx.alloc(), this.velocityCompressor, buf);
                final ByteBuf uncompressed = MoreByteBufUtils.preferredBuffer(ctx.alloc(), velocityCompressor, size);
                try {
                    this.velocityCompressor.inflate(compatibleIn, uncompressed, size);
                    out.add(uncompressed);
                    buf.clear();
                } catch (final Exception e) {
                    uncompressed.release();
                    throw e;
                } finally {
                    compatibleIn.release();
                }
            }
        }
    }
}
