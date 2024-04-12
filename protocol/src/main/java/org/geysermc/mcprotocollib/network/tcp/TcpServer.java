package org.geysermc.mcprotocollib.network.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.geysermc.mcprotocollib.network.AbstractServer;
import org.geysermc.mcprotocollib.network.packet.PacketProtocol;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class TcpServer extends AbstractServer {
    private Channel channel;
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
    private final TcpConnectionManager tcpManager;

    public TcpServer(String host, int port, Supplier<? extends PacketProtocol> protocol, TcpConnectionManager tcpManager) {
        super(host, port, protocol);
        this.tcpManager = tcpManager;
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

        ChannelFuture future = new ServerBootstrap()
            .channel(tcpManager.getServerSocketChannelClass())
            .childHandler(buildChannelInitializer())
            .group(tcpManager.getBossGroup(), tcpManager.getWorkerGroup())
            .localAddress(this.getHost(), this.getPort())
            .bind();

        if(wait) {
            try {
                future.sync();
            } catch(InterruptedException e) {
            }

            channel = future.channel();
            if(callback != null) {
                callback.run();
            }
        } else {
            future.addListener((ChannelFutureListener) future1 -> {
                if(future1.isSuccess()) {
                    channel = future1.channel();
                    if(callback != null) {
                        callback.run();
                    }
                } else {
                    LOGGER.error("Failed to asynchronously bind connection listener.", future1.cause());
                }
            });
        }
    }

    private ChannelInitializer<Channel> buildChannelInitializer() {
        return getGlobalFlag(MinecraftConstants.SERVER_CHANNEL_INITIALIZER, TcpServerChannelInitializer.DEFAULT_FACTORY)
            .create(this);
    }

    @Override
    public void closeImpl(boolean wait, final Runnable callback) {
        if(this.channel != null) {
            if(this.channel.isOpen()) {
                ChannelFuture future = this.channel.close();
                if(wait) {
                    try {
                        future.sync();
                    } catch(InterruptedException e) {
                    }

                    if(callback != null) {
                        callback.run();
                    }
                } else {
                    future.addListener((ChannelFutureListener) future1 -> {
                        if(future1.isSuccess()) {
                            if(callback != null) {
                                callback.run();
                            }
                        } else {
                            LOGGER.error("Failed to asynchronously close connection listener.", future1.cause());
                        }
                    });
                }
            }
            this.channel = null;
        }
    }
}
