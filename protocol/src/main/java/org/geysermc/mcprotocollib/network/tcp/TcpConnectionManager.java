package org.geysermc.mcprotocollib.network.tcp;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.uring.IoUringDatagramChannel;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;
import io.netty.channel.uring.IoUringSocketChannel;
import lombok.Data;
import org.geysermc.mcprotocollib.network.helper.TransportHelper;

import java.io.Closeable;

@Data
public class TcpConnectionManager implements Closeable {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final IoHandlerFactory ioHandlerFactory;
    private final Class<? extends Channel> channelClass;
    private final Class<? extends DatagramChannel> datagramChannelClass;
    private final Class<? extends ServerSocketChannel> serverSocketChannelClass;
    private final TransportHelper.TransportMethod transportMethod;

    public TcpConnectionManager() {
        this(0);
    }

    public TcpConnectionManager(int threads) {
        this.transportMethod = TransportHelper.determineTransportMethod();
        switch (this.transportMethod) {
            case IO_URING -> {
                this.ioHandlerFactory = IoUringIoHandler.newFactory();
                this.channelClass = IoUringSocketChannel.class;
                this.datagramChannelClass = IoUringDatagramChannel.class;
                this.serverSocketChannelClass = IoUringServerSocketChannel.class;
            }
            case EPOLL -> {
                this.ioHandlerFactory = EpollIoHandler.newFactory();
                this.channelClass = EpollSocketChannel.class;
                this.datagramChannelClass = EpollDatagramChannel.class;
                this.serverSocketChannelClass = EpollServerSocketChannel.class;
            }
            case NIO -> {
                this.ioHandlerFactory = NioIoHandler.newFactory();
                this.channelClass = NioSocketChannel.class;
                this.datagramChannelClass = NioDatagramChannel.class;
                this.serverSocketChannelClass = NioServerSocketChannel.class;
            }
            default -> throw new IllegalStateException("Unknown transport method: " + this.transportMethod);
        }
        this.bossGroup = new MultiThreadIoEventLoopGroup(threads, ioHandlerFactory);
        this.workerGroup = new MultiThreadIoEventLoopGroup(threads, ioHandlerFactory);
    }

    @Override
    public void close() {
        this.workerGroup.shutdownGracefully().awaitUninterruptibly();
        this.bossGroup.shutdownGracefully().awaitUninterruptibly();
    }
}
