package org.geysermc.mcprotocollib.network.tcp;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.codec.PacketCodecHelper;

import java.util.List;

/**
 * Decompresses incoming packets
 */
public class TcpPacketCompressionDecoder extends MessageToMessageDecoder<ByteBuf> {
    public static String ID = "compression-decoder";
    private static final int MAX_COMPRESSED_SIZE = 2097152;

    private final Session session;
    private final PacketCodecHelper codecHelper;
    private final boolean validateDecompression;
    private final VelocityCompressor compressor;

    public TcpPacketCompressionDecoder(Session session, boolean validateDecompression, final VelocityCompressor compressor) {
        this.session = session;
        this.codecHelper = session.getCodecHelper();
        this.validateDecompression = validateDecompression;
        this.compressor = compressor;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        try {
            if(in.readableBytes() != 0) {
                int size = codecHelper.readVarInt(in);
                if(size == 0) {
                    out.add(in.retain());
                } else {
                    if (validateDecompression) { // This is sectioned off as of at least Java Edition 1.18
                        if (size < this.session.getCompressionThreshold()) {
                            throw new DecoderException("Badly compressed packet: size of " + size + " is below threshold of " + this.session.getCompressionThreshold() + ".");
                        }

                        if (size > MAX_COMPRESSED_SIZE) {
                            throw new DecoderException("Badly compressed packet: size of " + size + " is larger than protocol maximum of " + MAX_COMPRESSED_SIZE + ".");
                        }
                    }

                    final ByteBuf compatibleIn = MoreByteBufUtils.ensureCompatible(ctx.alloc(), this.compressor, in);
                    final ByteBuf uncompressed = MoreByteBufUtils.preferredBuffer(ctx.alloc(), compressor, size);
                    try {
                        this.compressor.inflate(compatibleIn, uncompressed, size);
                        out.add(uncompressed);
                    } catch (final Exception e) {
                        uncompressed.release();
                        throw e;
                    } finally {
                        compatibleIn.release();
                    }
                }
            }
        } catch (final Throwable e) {
            if (!session.callPacketError(e)) {
                throw e;
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        compressor.close();
    }
}
