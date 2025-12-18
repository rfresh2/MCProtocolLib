package org.geysermc.mcprotocollib.network.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class TcpServerBootstrapInitializer {
    public static final Factory DEFAULT_FACTORY = TcpServerBootstrapInitializer::new;
    private final TcpServer server;
    private final ChannelInitializer<Channel> initializer;

    public static final Logger LOGGER = LoggerFactory.getLogger("Proxy");

    public TcpServerBootstrapInitializer(final TcpServer server, final ChannelInitializer<Channel> initializer) {
        this.server = server;
        this.initializer = initializer;
    }

    @FunctionalInterface
    public interface Factory {
        TcpServerBootstrapInitializer create(TcpServer server, ChannelInitializer<Channel> initializer);
    }

    public ServerBootstrap initBootstrap() {
        return new ServerBootstrap()
            .channel(server.getTcpManager().getServerSocketChannelClass())
            .childHandler(initializer)
            .group(server.getTcpManager().getBossGroup(), server.getTcpManager().getWorkerGroup());
    }
}
