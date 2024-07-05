package org.geysermc.mcprotocollib.network.tcp;

import com.velocitypowered.natives.encryption.VelocityCipher;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.geysermc.mcprotocollib.network.Session;

import java.util.List;

public class TcpPacketEncryptionEncoder extends MessageToMessageEncoder<ByteBuf> {
    public static String ID = "encryption-encoder";
    private final Session session;
    private final VelocityCipher cipher;

    public TcpPacketEncryptionEncoder(final Session session, final VelocityCipher cipher) {
        this.session = session;
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        try {
            ByteBuf compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), cipher, msg);
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
