package org.geysermc.mcprotocollib.network.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class TcpClientBootstrapInitializer {
    public static final Factory DEFAULT_FACTORY = TcpClientBootstrapInitializer::new;
    private final TcpClientSession client;
    private final ChannelInitializer<Channel> initializer;
    public static final Logger LOGGER = LoggerFactory.getLogger("Proxy");

    public TcpClientBootstrapInitializer(TcpClientSession client, ChannelInitializer<Channel> initializer) {
        this.client = client;
        this.initializer = initializer;
    }

    @FunctionalInterface
    public interface Factory {
        TcpClientBootstrapInitializer create(TcpClientSession client, ChannelInitializer<Channel> initializer);
    }

    public Bootstrap initBootstrap() {
        return new Bootstrap()
            .channel(client.getTcpManager().getChannelClass())
            .handler(initializer)
            .group(client.getTcpManager().getWorkerGroup())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, client.getConnectTimeout() * 1000);
    }
}
