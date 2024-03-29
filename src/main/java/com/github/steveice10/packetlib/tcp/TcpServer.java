package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.AbstractServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TcpServer extends AbstractServer {
    private Channel channel;
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
    @Setter
    private Consumer<Channel> initChannelConsumer;
    private final TcpConnectionManager tcpManager;

    public TcpServer(String host, int port, Supplier<? extends MinecraftProtocol> protocol, TcpConnectionManager tcpManager) {
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
        return new ChannelInitializer<>() {
            @Override
            public void initChannel(Channel channel) {
                InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
                MinecraftProtocol protocol = createPacketProtocol();

                TcpSession session = new TcpServerSession(address.getHostName(),
                                                          address.getPort(),
                                                          protocol,
                                                          TcpServer.this);
                session.getPacketProtocol().newServerSession(TcpServer.this, session);

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
                if (initChannelConsumer != null)
                    initChannelConsumer.accept(channel);
            }
        };
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
