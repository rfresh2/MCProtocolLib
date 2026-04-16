package org.geysermc.mcprotocollib.network.tcp;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.List;

/**
 * Decompresses incoming packets
 */
public class TcpPacketCompressionDecoder extends MessageToMessageDecoder<ByteBuf> {
    public static final String ID = "compression-decoder";
    private static final int SERVERBOUND_MAXIMUM_UNCOMPRESSED_SIZE = 2 * 1024 * 1024; // 2MiB
    private static final int VANILLA_MAXIMUM_UNCOMPRESSED_SIZE = 8 * 1024 * 1024; // 8MiB
    private static final int HARD_MAXIMUM_UNCOMPRESSED_SIZE = 128 * 1024 * 1024; // 128MiB

    private static final int CLIENTBOUND_UNCOMPRESSED_CAP =
        Boolean.getBoolean("mcpl.increased-compression-cap")
            ? HARD_MAXIMUM_UNCOMPRESSED_SIZE : VANILLA_MAXIMUM_UNCOMPRESSED_SIZE;
    private static final int SERVERBOUND_UNCOMPRESSED_CAP =
        Boolean.getBoolean("mcpl.increased-compression-cap")
            ? HARD_MAXIMUM_UNCOMPRESSED_SIZE : SERVERBOUND_MAXIMUM_UNCOMPRESSED_SIZE;
    private static final double MAX_COMPRESSION_RATIO = Double.parseDouble(System.getProperty("mcpl.max-compression-ratio", "64"));

    private final Session session;
    private final boolean validateDecompression;
    private final VelocityCompressor compressor;

    public TcpPacketCompressionDecoder(Session session, boolean validateDecompression, final VelocityCompressor compressor) {
        this.session = session;
        this.validateDecompression = validateDecompression;
        this.compressor = compressor;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        try {
            if(in.readableBytes() != 0) {
                int claimedUncompressedSize = MinecraftTypes.readVarInt(in);
                if(claimedUncompressedSize == 0) {
                    if (!validateDecompression) {
                        int actualUncompressedSize = in.readableBytes();
                        if (actualUncompressedSize >= session.getCompressionThreshold()) {
                            throw new CorruptedFrameException("Actual uncompressed size %s is greater than thresold %s".formatted(actualUncompressedSize, session.getCompressionThreshold()));
                        }
                    }
                    out.add(in.retain());
                    return;
                } else {
                    int length = in.readableBytes();
                    if (validateDecompression) { // This is sectioned off as of at least Java Edition 1.18
                        if (claimedUncompressedSize < this.session.getCompressionThreshold()) {
                            throw new DecoderException("Badly compressed packet: size of " + claimedUncompressedSize + " is below threshold of " + this.session.getCompressionThreshold() + ".");
                        }

                        if (session instanceof TcpClientSession) {
                            if (claimedUncompressedSize > CLIENTBOUND_UNCOMPRESSED_CAP) {
                                throw new CorruptedFrameException("Uncompressed size %s exceeds hard threshold of %s".formatted(claimedUncompressedSize, CLIENTBOUND_UNCOMPRESSED_CAP));
                            }
                        } else if (session instanceof TcpServerSession) {
                            if (claimedUncompressedSize > SERVERBOUND_UNCOMPRESSED_CAP) {
                                throw new CorruptedFrameException("Uncompressed size %s exceeds hard threshold of %s".formatted(claimedUncompressedSize, SERVERBOUND_UNCOMPRESSED_CAP));
                            }
                            double maxCompressedAllowed = length * MAX_COMPRESSION_RATIO;
                            if (claimedUncompressedSize > maxCompressedAllowed) {
                                throw new CorruptedFrameException("Uncompressed size %s exceeds ratio threshold of %s for compressed sized %s".formatted(claimedUncompressedSize, maxCompressedAllowed, length));
                            }
                        }
                    }

                    final ByteBuf compatibleIn = MoreByteBufUtils.ensureCompatible(ctx.alloc(), this.compressor, in);
                    final ByteBuf uncompressed = MoreByteBufUtils.preferredBuffer(ctx.alloc(), compressor, claimedUncompressedSize);
                    try {
                        this.compressor.inflate(compatibleIn, uncompressed, claimedUncompressedSize);
                        if (claimedUncompressedSize != uncompressed.writerIndex()) {
                            throw new CorruptedFrameException("Decompressed size %s does not match claimed uncompressed size %s".formatted(uncompressed.writerIndex(), claimedUncompressedSize));
                        }
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
