package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.packetlib.BuiltinFlags;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.codec.PacketCodecHelper;
import com.github.steveice10.packetlib.helper.TransportHelper;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.dns.*;
import io.netty.handler.codec.haproxy.*;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.incubator.channel.uring.IOUringDatagramChannel;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringSocketChannel;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadFactory;

public class TcpClientSession extends TcpSession {
    private static final String IP_REGEX = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";
    private static Class<? extends Channel> CHANNEL_CLASS;
    private static Class<? extends DatagramChannel> DATAGRAM_CHANNEL_CLASS;
    private static EventLoopGroup EVENT_LOOP_GROUP;
    private static final int WAIT_FOR_SHUTDOWN_IN_MS = 2000;
    private static Logger LOGGER = LoggerFactory.getLogger("PacketLib");

    private final String bindAddress;
    private final int bindPort;
    private final ProxyInfo proxy;
    private final PacketCodecHelper codecHelper;

    public TcpClientSession(String host, int port, PacketProtocol protocol) {
        this(host, port, protocol, null);
    }

    public TcpClientSession(String host, int port, PacketProtocol protocol, ProxyInfo proxy) {
        this(host, port, "0.0.0.0", 0, protocol, proxy);
    }

    public TcpClientSession(String host, int port, String bindAddress, int bindPort, PacketProtocol protocol) {
        this(host, port, bindAddress, bindPort, protocol, null);
    }

    public TcpClientSession(String host, int port, String bindAddress, int bindPort, PacketProtocol protocol, ProxyInfo proxy) {
        super(host, port, protocol);
        this.bindAddress = bindAddress;
        this.bindPort = bindPort;
        this.proxy = proxy;
        this.codecHelper = protocol.createHelper();
    }

    @Override
    public void connect(boolean wait) {
        if(this.disconnected) {
            throw new IllegalStateException("Session has already been disconnected.");
        }

        boolean debug = getFlag(BuiltinFlags.PRINT_DEBUG, false);

        if (CHANNEL_CLASS == null) {
            createTcpEventLoopGroup();
        }

        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(CHANNEL_CLASS);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel channel) {
                    PacketProtocol protocol = getPacketProtocol();
                    protocol.newClientSession(TcpClientSession.this);

                    channel.config().setOption(ChannelOption.IP_TOS, 0x18);
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                    } catch (ChannelException e) {
                        if(debug) {
                            LOGGER.debug("Exception while trying to set TCP_NODELAY", e);
                        }
                    }

                    ChannelPipeline pipeline = channel.pipeline();

                    refreshReadTimeoutHandler(channel);
                    refreshWriteTimeoutHandler(channel);

                    addProxy(pipeline);

                    int size = protocol.getPacketHeader().getLengthSize();
                    if (size > 0) {
                        pipeline.addLast("sizer", new TcpPacketSizer(TcpClientSession.this, size));
                    }

                    pipeline.addLast("codec", new TcpPacketCodec(TcpClientSession.this, true));
                    pipeline.addLast("manager", TcpClientSession.this);

                    addHAProxySupport(pipeline);
                }
            }).group(EVENT_LOOP_GROUP).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout() * 1000);

            InetSocketAddress remoteAddress = resolveAddress();
            bootstrap.remoteAddress(remoteAddress);
            bootstrap.localAddress(bindAddress, bindPort);

            ChannelFuture future = bootstrap.connect();
            if (wait) {
                future.sync();
            }

            future.addListener((futureListener) -> {
                if (!futureListener.isSuccess()) {
                    exceptionCaught(null, futureListener.cause());
                }
            });
        } catch(Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public PacketCodecHelper getCodecHelper() {
        return this.codecHelper;
    }

    private InetSocketAddress resolveAddress() {
        boolean debug = getFlag(BuiltinFlags.PRINT_DEBUG, false);

        String name = this.getPacketProtocol().getSRVRecordPrefix() + "._tcp." + this.getHost();
        if (debug) {
            LOGGER.debug("Attempting SRV lookup for \"" + name + "\".");
        }

        if(getFlag(BuiltinFlags.ATTEMPT_SRV_RESOLVE, true) && (!this.host.matches(IP_REGEX) && !this.host.equalsIgnoreCase("localhost"))) {
            DnsNameResolver resolver = null;
            AddressedEnvelope<DnsResponse, InetSocketAddress> envelope = null;
            try {
                resolver = new DnsNameResolverBuilder(EVENT_LOOP_GROUP.next())
                    .channelType(DATAGRAM_CHANNEL_CLASS)
                    .build();
                envelope = resolver.query(new DefaultDnsQuestion(name, DnsRecordType.SRV)).get();

                DnsResponse response = envelope.content();
                if (response.count(DnsSection.ANSWER) > 0) {
                    DefaultDnsRawRecord record = response.recordAt(DnsSection.ANSWER, 0);
                    if (record.type() == DnsRecordType.SRV) {
                        ByteBuf buf = record.content();
                        buf.skipBytes(4); // Skip priority and weight.

                        int port = buf.readUnsignedShort();
                        String host = DefaultDnsRecordDecoder.decodeName(buf);
                        if (host.endsWith(".")) {
                            host = host.substring(0, host.length() - 1);
                        }

                        if(debug) {
                            LOGGER.debug("Found SRV record containing \"" + host + ":" + port + "\".");
                        }

                        this.host = host;
                        this.port = port;
                    } else if (debug) {
                        LOGGER.debug("Received non-SRV record in response.");
                    }
                } else if (debug) {
                    LOGGER.debug("No SRV record found.");
                }
            } catch(Exception e) {
                if (debug) {
                    LOGGER.debug("Failed to resolve SRV record.", e);
                }
            } finally {
                if (envelope != null) {
                    envelope.release();
                }

                if (resolver != null) {
                    resolver.close();
                }
            }
        } else if(debug) {
            LOGGER.debug("Not resolving SRV record for " + this.host);
        }

        // Resolve host here
        try {
            InetAddress resolved = InetAddress.getByName(getHost());
            if (debug) {
                LOGGER.debug("Resolved {} -> {}", getHost(), resolved.getHostAddress());
            }
            return new InetSocketAddress(resolved, getPort());
        } catch (UnknownHostException e) {
            if (debug) {
                LOGGER.debug("Failed to resolve host, letting Netty do it instead.", e);
            }
            return InetSocketAddress.createUnresolved(getHost(), getPort());
        }
    }

    private void addProxy(ChannelPipeline pipeline) {
        if(proxy != null) {
            switch(proxy.getType()) {
                case HTTP:
                    if (proxy.isAuthenticated()) {
                        pipeline.addFirst("proxy", new HttpProxyHandler(proxy.getAddress(), proxy.getUsername(), proxy.getPassword()));
                    } else {
                        pipeline.addFirst("proxy", new HttpProxyHandler(proxy.getAddress()));
                    }

                    break;
                case SOCKS4:
                    if (proxy.isAuthenticated()) {
                        pipeline.addFirst("proxy", new Socks4ProxyHandler(proxy.getAddress(), proxy.getUsername()));
                    } else {
                        pipeline.addFirst("proxy", new Socks4ProxyHandler(proxy.getAddress()));
                    }

                    break;
                case SOCKS5:
                    if (proxy.isAuthenticated()) {
                        pipeline.addFirst("proxy", new Socks5ProxyHandler(proxy.getAddress(), proxy.getUsername(), proxy.getPassword()));
                    } else {
                        pipeline.addFirst("proxy", new Socks5ProxyHandler(proxy.getAddress()));
                    }

                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported proxy type: " + proxy.getType());
            }
        }
    }

    private void addHAProxySupport(ChannelPipeline pipeline) {
        InetSocketAddress clientAddress = getFlag(BuiltinFlags.CLIENT_PROXIED_ADDRESS);
        if (getFlag(BuiltinFlags.ENABLE_CLIENT_PROXY_PROTOCOL, false) && clientAddress != null) {
            pipeline.addFirst("proxy-protocol-packet-sender", new ChannelInboundHandlerAdapter() {
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    HAProxyProxiedProtocol proxiedProtocol = clientAddress.getAddress() instanceof Inet4Address ? HAProxyProxiedProtocol.TCP4 : HAProxyProxiedProtocol.TCP6;
                    InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                    ctx.channel().writeAndFlush(new HAProxyMessage(
                        HAProxyProtocolVersion.V2, HAProxyCommand.PROXY, proxiedProtocol,
                        clientAddress.getAddress().getHostAddress(), remoteAddress.getAddress().getHostAddress(),
                        clientAddress.getPort(), remoteAddress.getPort()
                    ));
                    ctx.pipeline().remove(this);
                    ctx.pipeline().remove("proxy-protocol-encoder");
                    super.channelActive(ctx);
                }
            });
            pipeline.addFirst("proxy-protocol-encoder", HAProxyMessageEncoder.INSTANCE);
        }
    }

    @Override
    public void disconnect(String reason, Throwable cause) {
        super.disconnect(reason, cause);
    }

    private static void createTcpEventLoopGroup() {
        if (CHANNEL_CLASS != null) {
            return;
        }

        switch (TransportHelper.determineTransportMethod()) {
            case IO_URING:
                EVENT_LOOP_GROUP = new IOUringEventLoopGroup(newThreadFactory());
                CHANNEL_CLASS = IOUringSocketChannel.class;
                DATAGRAM_CHANNEL_CLASS = IOUringDatagramChannel.class;
                break;
            case EPOLL:
                EVENT_LOOP_GROUP = new EpollEventLoopGroup(newThreadFactory());
                CHANNEL_CLASS = EpollSocketChannel.class;
                DATAGRAM_CHANNEL_CLASS = EpollDatagramChannel.class;
                break;
            case NIO:
                EVENT_LOOP_GROUP = new NioEventLoopGroup(newThreadFactory());
                CHANNEL_CLASS = NioSocketChannel.class;
                DATAGRAM_CHANNEL_CLASS = NioDatagramChannel.class;
                break;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> EVENT_LOOP_GROUP.shutdownGracefully().awaitUninterruptibly(WAIT_FOR_SHUTDOWN_IN_MS)));
    }

    protected static ThreadFactory newThreadFactory() {
       // Create a new daemon thread. When the last non daemon thread ends
       // the runtime environment will call the shutdown hooks. One of the
       // hooks will try to shut down the event loop group which will
       // normally lead to the thread exiting. If not, it will be forcably
       // killed after WAIT_FOR_SHUTDOWN_IN_MS ms along with the other
       // daemon threads as the runtime exits.
       return new DefaultThreadFactory(TcpClientSession.class, true);
    }
}
