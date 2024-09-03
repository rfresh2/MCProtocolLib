package org.geysermc.mcprotocollib.network.tcp;

import com.velocitypowered.natives.encryption.JavaVelocityCipher;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;

import java.util.List;

@ChannelHandler.Sharable
public class TcpPacketSizeEncoder extends MessageToMessageEncoder<ByteBuf> {
    public static String ID = "size-encoder";

    public static final boolean USE_HEAP_BUF = Natives.cipher.get() == JavaVelocityCipher.FACTORY;
    private final Session session;

    public TcpPacketSizeEncoder(Session session) {
        this.session = session;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf,
                          List<Object> list) throws Exception {
        try {
            final int length = buf.readableBytes();
            var varIntBytesLen = MinecraftConstants.PACKET_HEADER.getLengthSize(length);

            final ByteBuf lenBuf = USE_HEAP_BUF
                ? ctx.alloc().heapBuffer(varIntBytesLen)
                : ctx.alloc().directBuffer(varIntBytesLen);

            MinecraftConstants.PACKET_HEADER.writeLength(lenBuf, MinecraftCodecHelper.INSTANCE, length);
            list.add(lenBuf);
            list.add(buf.retain());
        } catch (final Exception e) {
            if (!session.callPacketError(e)) {
                throw e;
            }
        }
    }
}
