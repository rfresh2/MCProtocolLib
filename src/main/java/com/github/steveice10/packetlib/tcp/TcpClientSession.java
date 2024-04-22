package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.BuiltinFlags;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.codec.PacketCodecHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.dns.*;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class TcpClientSession extends TcpSession {
    private static final String IP_REGEX = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";
    /**
     * See {@link EventLoopGroup#shutdownGracefully(long, long, TimeUnit)}
     */
    private static final int SHUTDOWN_QUIET_PERIOD_MS = 100;
    private static final int SHUTDOWN_TIMEOUT_MS = 500;
    private static Logger LOGGER = LoggerFactory.getLogger("Proxy");

    private final String bindAddress;
    private final int bindPort;
    private final ProxyInfo proxy;
    private final PacketCodecHelper codecHelper;
    @Getter private final TcpConnectionManager tcpManager;

    public TcpClientSession(String host, int port, MinecraftProtocol protocol, TcpConnectionManager tcpManager) {
        this(host, port, protocol, null, tcpManager);
    }

    public TcpClientSession(String host, int port, MinecraftProtocol protocol, ProxyInfo proxy, TcpConnectionManager tcpManager) {
        this(host, port, "0.0.0.0", 0, protocol, proxy, tcpManager);
    }

    public TcpClientSession(String host, int port, String bindAddress, int bindPort, MinecraftProtocol protocol, TcpConnectionManager tcpManager) {
        this(host, port, bindAddress, bindPort, protocol, null, tcpManager);
    }

    public TcpClientSession(String host, int port, String bindAddress, int bindPort, MinecraftProtocol protocol, ProxyInfo proxy, TcpConnectionManager tcpManager) {
        super(host, port, protocol);
        this.bindAddress = bindAddress;
        this.bindPort = bindPort;
        this.proxy = proxy;
        this.codecHelper = protocol.createHelper();
        this.tcpManager = tcpManager;
    }

    public ChannelInitializer<Channel> buildChannelInitializer(boolean transferring) {
        return getFlag(MinecraftConstants.CLIENT_CHANNEL_INITIALIZER, TcpClientChannelInitializer.DEFAULT_FACTORY)
            .create(this, transferring);
    }

    public Bootstrap buildBootstrap(final ChannelInitializer<Channel> initializer) {
        return new Bootstrap()
            .channel(tcpManager.getChannelClass())
            .handler(initializer)
            .group(tcpManager.getWorkerGroup())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout() * 1000);
    }

    @Override
    public void connect(boolean wait, boolean transferring) {
        connect(wait, transferring, buildBootstrap(buildChannelInitializer(transferring)));
    }

    public void connect(boolean wait, boolean transferring, Bootstrap bootstrap) {
        if(this.disconnected) {
            throw new IllegalStateException("Session has already been disconnected.");
        }

        try {
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
                resolver = new DnsNameResolverBuilder(tcpManager.getWorkerGroup().next())
                    .channelType(tcpManager.getDatagramChannelClass())
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

    @Override
    public void disconnect(String reason, Throwable cause) {
        super.disconnect(reason, cause);
    }
}
