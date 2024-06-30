package org.geysermc.mcprotocollib.network.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

import java.net.InetSocketAddress;

public class TcpServerChannelInitializer extends ChannelInitializer<Channel> {
    public static final Factory DEFAULT_FACTORY = TcpServerChannelInitializer::new;

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
        MinecraftProtocol protocol = (MinecraftProtocol) server.createPacketProtocol();

        TcpSession session = new TcpServerSession(address.getHostName(),
                                                  address.getPort(),
                                                  protocol,
                                                  server);
        session.getPacketProtocol().newServerSession(server, session);

        channel.config().setOption(ChannelOption.IP_TOS, 0x18);
        try {
            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
        } catch (ChannelException ignored) {
        }

        ChannelPipeline pipeline = channel.pipeline();

        session.refreshReadTimeoutHandler(channel);
        session.refreshWriteTimeoutHandler(channel);

        pipeline
            .addLast(TcpPacketSizeDecoder.ID, new TcpPacketSizeDecoder())
            .addLast(TcpPacketSizeEncoder.ID, new TcpPacketSizeEncoder(session))
            .addLast(TcpPacketCodec.ID, new TcpPacketCodec(session, false))
            .addLast(TcpSession.ID, session);
    }
}
