package com.github.steveice10.packetlib.tcp;

import com.velocitypowered.natives.encryption.VelocityCipher;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class TcpPacketEncryptionDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final VelocityCipher cipher;

    public TcpPacketEncryptionDecoder(final VelocityCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        ByteBuf compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), cipher, in).slice();
        try {
            cipher.process(compatible);
            out.add(compatible);
        } catch (Exception e) {
            compatible.release();
            throw e;
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        cipher.close();
    }
}
