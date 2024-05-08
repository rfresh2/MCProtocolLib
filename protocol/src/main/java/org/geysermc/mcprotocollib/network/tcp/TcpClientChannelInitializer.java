package org.geysermc.mcprotocollib.network.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.geysermc.mcprotocollib.network.BuiltinFlags;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

public class TcpClientChannelInitializer extends ChannelInitializer<Channel> {
    public static final Factory DEFAULT_FACTORY = TcpClientChannelInitializer::new;
    private final TcpClientSession client;

    public TcpClientChannelInitializer(TcpClientSession client) {
        this.client = client;
    }

    @FunctionalInterface
    public interface Factory {
        TcpClientChannelInitializer create(TcpClientSession client);
    }

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        var protocol = client.getPacketProtocol();
        protocol.newClientSession(client);

        channel.config().setOption(ChannelOption.IP_TOS, 0x18);
        try {
            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
        } catch (ChannelException e) {
            // fall through
        }

        ChannelPipeline pipeline = channel.pipeline();

        client.refreshReadTimeoutHandler(channel);
        client.refreshWriteTimeoutHandler(channel);

        addProxy(pipeline);

        pipeline
            .addLast("size-decoder", new TcpPacketSizeDecoder(client))
            .addLast("size-encoder", new TcpPacketSizeEncoder(client))
            .addLast("codec", new TcpPacketCodec(client, true))
            .addLast("manager", client);

        addHAProxySupport(pipeline);
    }

    private void addProxy(ChannelPipeline pipeline) {
        var proxy = client.getProxy();
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
        InetSocketAddress clientAddress = client.getFlag(BuiltinFlags.CLIENT_PROXIED_ADDRESS);
        if (client.getFlag(BuiltinFlags.ENABLE_CLIENT_PROXY_PROTOCOL, false) && clientAddress != null) {
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
}
