package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.packetlib.Session;
import com.velocitypowered.natives.encryption.JavaVelocityCipher;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TcpPacketSizeEncoder extends MessageToByteEncoder<ByteBuf> {

    public static final boolean USE_HEAP_BUF = Natives.cipher.get() == JavaVelocityCipher.FACTORY;
    private final Session session;

    public TcpPacketSizeEncoder(Session session) {
        this.session = session;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        session.getPacketProtocol().getPacketHeader().writeLength(out, session.getCodecHelper(), msg.readableBytes());
        out.writeBytes(msg);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect)
        throws Exception {
        var size = session.getPacketProtocol().getPacketHeader().getLengthSize(msg.readableBytes()) + msg.readableBytes();
        return USE_HEAP_BUF
            ? ctx.alloc().heapBuffer(size)
            : ctx.alloc().directBuffer(size);
    }
}
