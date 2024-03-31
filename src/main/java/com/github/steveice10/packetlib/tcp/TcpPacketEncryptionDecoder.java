package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.packetlib.Session;
import com.velocitypowered.natives.encryption.VelocityCipher;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class TcpPacketEncryptionDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Session session;
    private final VelocityCipher cipher;

    public TcpPacketEncryptionDecoder(final Session session, final VelocityCipher cipher) {
        this.session = session;
        this.cipher = cipher;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        try {
            ByteBuf compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), cipher, in).slice();
            try {
                cipher.process(compatible);
                out.add(compatible);
            } catch (Exception e) {
                compatible.release();
                throw e;
            }
        } catch (final Throwable e) {
            if (!session.callPacketError(e)) {
                throw e;
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        cipher.close();
    }
}
