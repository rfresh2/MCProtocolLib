package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.packetlib.helper.TransportHelper;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.incubator.channel.uring.IOUringDatagramChannel;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;
import io.netty.incubator.channel.uring.IOUringSocketChannel;
import lombok.Data;

import java.io.Closeable;

@Data
public class TcpConnectionManager implements Closeable {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Class<? extends Channel> channelClass;
    private final Class<? extends DatagramChannel> datagramChannelClass;
    private final Class<? extends ServerSocketChannel> serverSocketChannelClass;
    private final TransportHelper.TransportMethod transportMethod;

    public TcpConnectionManager() {
        this.transportMethod = TransportHelper.determineTransportMethod();
        switch (this.transportMethod) {
            case IO_URING:
                this.bossGroup = new IOUringEventLoopGroup();
                this.workerGroup = new IOUringEventLoopGroup();
                this.channelClass = IOUringSocketChannel.class;
                this.datagramChannelClass = IOUringDatagramChannel.class;
                this.serverSocketChannelClass = IOUringServerSocketChannel.class;
                break;
            case EPOLL:
                this.bossGroup = new EpollEventLoopGroup();
                this.workerGroup = new EpollEventLoopGroup();
                this.channelClass = EpollSocketChannel.class;
                this.datagramChannelClass = EpollDatagramChannel.class;
                this.serverSocketChannelClass = EpollServerSocketChannel.class;
                break;
            case NIO:
                this.bossGroup = new NioEventLoopGroup();
                this.workerGroup = new NioEventLoopGroup();
                this.channelClass = NioSocketChannel.class;
                this.datagramChannelClass = NioDatagramChannel.class;
                this.serverSocketChannelClass = NioServerSocketChannel.class;
                break;
            default:
                throw new IllegalStateException("Unknown transport method: " + this.transportMethod);
        }
    }

    @Override
    public void close() {
        this.workerGroup.shutdownGracefully().awaitUninterruptibly();
        this.bossGroup.shutdownGracefully().awaitUninterruptibly();
    }
}
