package org.geysermc.mcprotocollib.network.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.geysermc.mcprotocollib.network.AbstractServer;
import org.geysermc.mcprotocollib.network.packet.PacketProtocol;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TcpServer extends AbstractServer {
    private Channel channel;
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
    private final TcpConnectionManager tcpManager;
    private final TcpServerSessionBuilder sessionBuilder;

    public TcpServer(String host, int port, Supplier<? extends PacketProtocol> protocol, TcpConnectionManager tcpManager) {
        super(host, port, protocol);
        this.tcpManager = tcpManager;
        this.sessionBuilder = defaultSessionBuilder();
    }

    public TcpServer(String host, int port, Supplier<? extends PacketProtocol> protocol, TcpConnectionManager tcpManager, TcpServerSessionBuilder sessionBuilder) {
        super(host, port, protocol);
        this.tcpManager = tcpManager;
        this.sessionBuilder = sessionBuilder;
    }

    @FunctionalInterface
    public interface TcpServerSessionBuilder {
        TcpServerSession createSession(InetSocketAddress address);
    }

    public TcpServerSession createSession(InetSocketAddress address) {
        return this.sessionBuilder.createSession(address);
    }

    private TcpServerSessionBuilder defaultSessionBuilder() {
        return address -> new TcpServerSession(address.getHostName(), address.getPort(), (MinecraftProtocol) createPacketProtocol(), null);
    }

    @Override
    public boolean isListening() {
        return this.channel != null && this.channel.isOpen();
    }

    @Override
    public void bindImpl(boolean wait, final Runnable callback) {
        if(this.channel != null) {
            return;
        }

        var bootstrap = new ServerBootstrap()
            .channel(tcpManager.getServerSocketChannelClass())
            .childHandler(buildChannelInitializer())
            .group(tcpManager.getBossGroup(), tcpManager.getWorkerGroup())
            .localAddress(this.getHost(), this.getPort());

        CompletableFuture<Void> handleFuture = new CompletableFuture<>();
        bootstrap.bind().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channel = future.channel();
                if (callback != null) {
                    callback.run();
                }
            } else {
                LOGGER.error("Failed to bind connection listener.", future.cause());
            }

            handleFuture.complete(null);
        });

        if (wait) {
            handleFuture.join();
        }
    }

    private ChannelInitializer<Channel> buildChannelInitializer() {
        return getGlobalFlag(MinecraftConstants.SERVER_CHANNEL_INITIALIZER, TcpServerChannelInitializer.DEFAULT_FACTORY)
            .create(this);
    }

    @Override
    public void closeImpl(boolean wait, final Runnable callback) {
        if (this.channel != null) {
            if (this.channel.isOpen()) {
                CompletableFuture<Void> handleFuture = new CompletableFuture<>();
                this.channel.close().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        if (callback != null) {
                            callback.run();
                        }
                    } else {
                        LOGGER.error("Failed to close connection listener.", future.cause());
                    }

                    handleFuture.complete(null);
                });

                if (wait) {
                    handleFuture.join();
                }
            }

            this.channel = null;
        }
    }
}
