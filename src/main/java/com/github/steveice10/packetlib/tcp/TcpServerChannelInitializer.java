package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import io.netty.channel.*;

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
        MinecraftProtocol protocol = server.createPacketProtocol();

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
            .addLast("size-decoder", new TcpPacketSizeDecoder())
            .addLast("size-encoder", new TcpPacketSizeEncoder(session))
            .addLast("codec", new TcpPacketCodec(session, false))
            .addLast("manager", session);
    }
}
