package org.geysermc.mcprotocollib.network.tcp;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.encryption.JavaVelocityCipher;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.codec.PacketCodecHelper;

import java.util.zip.DataFormatException;

public class TcpPacketCompressionAndSizeEncoder extends MessageToByteEncoder<ByteBuf> {
    public static String ID = "compression-encoder";
    public static final boolean USE_HEAP_BUF = Natives.cipher.get() == JavaVelocityCipher.FACTORY;
    private final Session session;
    private final PacketCodecHelper codecHelper;
    private final VelocityCompressor compressor;

    public TcpPacketCompressionAndSizeEncoder(Session session, VelocityCompressor compressor) {
        this.session = session;
        this.codecHelper = session.getCodecHelper();
        this.compressor = compressor;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf in, final ByteBuf out) throws Exception {
        try {
            int uncompressed = in.readableBytes();
            if(uncompressed < this.session.getCompressionThreshold()) {
                codecHelper.writeVarInt(out, uncompressed + 1);
                codecHelper.writeVarInt(out, 0);
                out.writeBytes(in);
            } else {
                codecHelper.write21BitVarInt(out, 0); // Dummy packet length
                codecHelper.writeVarInt(out, uncompressed);
                ByteBuf compatibleIn = MoreByteBufUtils.ensureCompatible(ctx.alloc(), compressor, in);

                int startCompressed = out.writerIndex();
                try {
                    compressor.deflate(compatibleIn, out);
                } finally {
                    compatibleIn.release();
                }
                int compressedLength = out.writerIndex() - startCompressed;
                if (compressedLength >= 1 << 21) {
                    throw new DataFormatException("The server sent a very large (over 2MiB compressed) packet.");
                }

                int writerIndex = out.writerIndex();
                int packetLength = out.readableBytes() - 3;
                out.writerIndex(0);
                codecHelper.write21BitVarInt(out, packetLength); // write actual packet length
                out.writerIndex(writerIndex);
            }
        } catch (final Throwable e) {
            if (!session.callPacketError(e)) {
                throw e;
            }
        }

    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect)
        throws Exception {
        int uncompressed = msg.readableBytes();
        if (uncompressed < session.getCompressionThreshold()) {
            int finalBufferSize = uncompressed + 1;
            finalBufferSize += session.getPacketProtocol().getPacketHeader().getLengthSize(finalBufferSize);
            return USE_HEAP_BUF
                ? ctx.alloc().heapBuffer(finalBufferSize)
                : ctx.alloc().directBuffer(finalBufferSize);
        }

        // (maximum data length after compression) + packet length varint + uncompressed data varint

        int initialBufferSize = (uncompressed - 1) + 3 + session.getPacketProtocol().getPacketHeader().getLengthSize(uncompressed);
        return MoreByteBufUtils.preferredBuffer(ctx.alloc(), compressor, initialBufferSize);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        compressor.close();
    }
}
