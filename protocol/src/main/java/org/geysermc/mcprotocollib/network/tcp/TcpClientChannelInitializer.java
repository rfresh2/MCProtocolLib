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
import org.geysermc.mcprotocollib.network.packet.PacketProtocol;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import static org.geysermc.mcprotocollib.network.tcp.TcpSession.HA_PROXY_ENCODER_ID;
import static org.geysermc.mcprotocollib.network.tcp.TcpSession.PROXY_HANDLER_ID;

public class TcpClientChannelInitializer extends ChannelInitializer<Channel> {
    public static final Factory DEFAULT_FACTORY = TcpClientChannelInitializer::new;
    private final TcpClientSession client;
    private final boolean transferring;

    public TcpClientChannelInitializer(TcpClientSession client, boolean transferring) {
        this.client = client;
        this.transferring = transferring;
    }

    @FunctionalInterface
    public interface Factory {
        TcpClientChannelInitializer create(TcpClientSession client, boolean transferring);
    }

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        PacketProtocol protocol = client.getPacketProtocol();
        protocol.newClientSession(client, transferring);

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
            .addLast(TcpPacketSizeDecoder.ID, new TcpPacketSizeDecoder())
            .addLast(TcpPacketSizeEncoder.ID, new TcpPacketSizeEncoder(client))
            .addLast(TcpPacketCodec.ID, new TcpPacketCodec(client, true))
            .addLast(FlushHandler.ID, new FlushHandler())
            .addLast(TcpSession.ID, client);

        addHAProxySupport(pipeline);
    }

    private void addProxy(ChannelPipeline pipeline) {
        var proxy = client.getProxy();
        if (proxy != null) {
            switch (proxy.type()) {
                case HTTP -> {
                    if (proxy.username() != null && proxy.password() != null) {
                        pipeline.addFirst(PROXY_HANDLER_ID, new HttpProxyHandler(proxy.address(), proxy.username(), proxy.password()));
                    } else {
                        pipeline.addFirst(PROXY_HANDLER_ID, new HttpProxyHandler(proxy.address()));
                    }
                }
                case SOCKS4 -> {
                    if (proxy.username() != null) {
                        pipeline.addFirst(PROXY_HANDLER_ID, new Socks4ProxyHandler(proxy.address(), proxy.username()));
                    } else {
                        pipeline.addFirst(PROXY_HANDLER_ID, new Socks4ProxyHandler(proxy.address()));
                    }
                }
                case SOCKS5 -> {
                    if (proxy.username() != null && proxy.password() != null) {
                        pipeline.addFirst(PROXY_HANDLER_ID, new Socks5ProxyHandler(proxy.address(), proxy.username(), proxy.password()));
                    } else {
                        pipeline.addFirst(PROXY_HANDLER_ID, new Socks5ProxyHandler(proxy.address()));
                    }
                }
                default -> throw new UnsupportedOperationException("Unsupported proxy type: " + proxy.type());
            }
        }
    }

    private void addHAProxySupport(ChannelPipeline pipeline) {
        InetSocketAddress clientAddress = client.getFlag(BuiltinFlags.CLIENT_PROXIED_ADDRESS);
        if (client.getFlag(BuiltinFlags.ENABLE_CLIENT_PROXY_PROTOCOL, false) && clientAddress != null) {
            pipeline.addFirst(HA_PROXY_ENCODER_ID, new ChannelInboundHandlerAdapter() {
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
                    ctx.pipeline().remove(HA_PROXY_ENCODER_ID);
                    super.channelActive(ctx);
                }
            });
            pipeline.addFirst(HA_PROXY_ENCODER_ID, HAProxyMessageEncoder.INSTANCE);
        }
    }
}
