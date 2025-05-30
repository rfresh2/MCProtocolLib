package org.geysermc.mcprotocollib.network.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class TcpServerChannelInitializer extends ChannelInitializer<Channel> {
    public static final Factory DEFAULT_FACTORY = TcpServerChannelInitializer::new;
    private static final Logger LOGGER = LoggerFactory.getLogger("Proxy");

    private final TcpServer server;

    public TcpServerChannelInitializer(TcpServer server) {
        this.server = server;
    }

    public interface Factory {
        TcpServerChannelInitializer create(TcpServer server);
    }

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        TcpSession session = server.createSession(address);
        session.getPacketProtocol().newServerSession(server, session);

        try {
            channel.config().setOption(ChannelOption.WRITE_BUFFER_WATER_MARK, MinecraftConstants.WRITE_BUFFER_WATER_MARK);
            channel.config().setOption(ChannelOption.IP_TOS, 0x18);
            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
        } catch (Throwable e) {
            LOGGER.debug("Failed setting server channel options", e);
        }

        ChannelPipeline pipeline = channel.pipeline();

        session.refreshReadTimeoutHandler(channel);
        session.refreshWriteTimeoutHandler(channel);

        pipeline
            .addLast(TcpPacketSizeDecoder.ID, new TcpPacketSizeDecoder())
            .addLast(TcpPacketSizeEncoder.ID, new TcpPacketSizeEncoder(session))
            .addLast(AutoReadFlowControlHandler.ID, new AutoReadFlowControlHandler())
            .addLast(TcpPacketCodec.ID, new TcpPacketCodec(session, false))
            .addLast(FlushHandler.ID, new FlushHandler())
            .addLast(TcpSession.ID, session);
    }
}
