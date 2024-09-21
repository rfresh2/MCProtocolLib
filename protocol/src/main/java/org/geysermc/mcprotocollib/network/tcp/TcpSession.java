package org.geysermc.mcprotocollib.network.tcp;

import com.google.common.util.concurrent.Futures;
import com.velocitypowered.natives.util.Natives;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.network.Flag;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionListener;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundDelimiterPacket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public abstract class TcpSession extends SimpleChannelInboundHandler<Packet> implements Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSession.class);
    public static String ID = "manager";
    public static String READ_TIMEOUT_HANDLER_ID = "readTimeout";
    public static String WRITE_TIMEOUT_HANDLER_ID = "writeTimeout";
    public static String PROXY_HANDLER_ID = "proxy";
    public static String HA_PROXY_ENCODER_ID = "proxy-protocol-packet-sender";
    protected String host;
    protected int port;
    private final MinecraftProtocol protocol;
    private int compressionThreshold = -1;
    private int connectTimeout = 30;
    private int readTimeout = 30;
    private int writeTimeout = 0;

    private final Map<String, Object> flags = new HashMap<>();
    private SessionListener[] listeners = new SessionListener[0];

    private Channel channel;
    protected boolean disconnected = false;

    public TcpSession(String host, int port, MinecraftProtocol protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    @Override
    public void connect() {
        this.connect(true);
    }

    @Override
    public void connect(boolean wait) {
        this.connect(wait, false);
    }

    @Override
    public void connect(boolean wait, boolean transferring) {
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.channel != null ? this.channel.localAddress() : null;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.channel != null ? this.channel.remoteAddress() : null;
    }

    @Override
    public MinecraftProtocol getPacketProtocol() {
        return this.protocol;
    }

    @Override
    public Map<String, Object> getFlags() {
        return Collections.unmodifiableMap(this.flags);
    }

    @Override
    public boolean hasFlag(Flag<?> flag) {
        return this.flags.containsKey(flag.key());
    }

    @Override
    public <T> T getFlag(Flag<T> flag) {
        return this.getFlag(flag, null);
    }

    @Override
    public <T> T getFlag(Flag<T> flag, T def) {
        Object value = this.flags.get(flag.key());
        if (value == null) {
            return def;
        }

        try {
            return flag.cast(value);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Tried to get flag \"" + flag.key() + "\" as the wrong type. Actual type: " + value.getClass().getName());
        }
    }

    @Override
    public <T> void setFlag(Flag<T> flag, T value) {
        this.flags.put(flag.key(), value);
    }

    @Override
    public void setFlags(Map<String, Object> flags) {
        this.flags.putAll(flags);
    }

    @Override
    public List<SessionListener> getListeners() {
        return Arrays.asList(this.listeners);
    }

    @Override
    public synchronized void addListener(SessionListener listener) {
        final SessionListener[] newListeners = Arrays.copyOf(this.listeners, this.listeners.length + 1);
        newListeners[newListeners.length - 1] = listener;
        this.listeners = newListeners;
    }

    @Override
    public void removeListener(SessionListener listener) {
        if (this.listeners.length == 0) return;
        int i = -1;
        for (int j = 0; j < this.listeners.length; j++) {
            if (this.listeners[j] == listener) {
                i = j;
                break;
            }
        }
        if (i == -1) return;
        final SessionListener[] newListeners = new SessionListener[this.listeners.length - 1];
        System.arraycopy(this.listeners, 0, newListeners, 0, i);
        System.arraycopy(this.listeners, i + 1, newListeners, i, this.listeners.length - i - 1);
        this.listeners = newListeners;
    }

    @Override
    public Packet callPacketSending(final Packet packet) {
        Packet toSend = packet;
        try {
            for (int i = 0; i < listeners.length; i++) {
                var listener = listeners[i];
                toSend = listener.packetSending(this, toSend);
                // short circuit posting to other listeners if its cancelled
                if (toSend == null) break;
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
        return toSend;
    }

    @Override
    public void callConnected() {
        try {
            for (int i = 0; i < listeners.length; i++) {
                var listener = listeners[i];
                listener.connected(this);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public void callDisconnecting(final Component reason, final Throwable cause) {
        try {
            for (int i = 0; i < listeners.length; i++) {
                var listener = listeners[i];
                listener.disconnecting(this, reason, cause);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public void callDisconnected(final Component reason, final Throwable cause) {
        try {
            for (int i = 0; i < listeners.length; i++) {
                var listener = listeners[i];
                listener.disconnected(this, reason, cause);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public boolean callPacketError(final Throwable throwable) {
        boolean suppress = false;
        try {
            for (int i = 0; i < listeners.length; i++) {
                var listener = listeners[i];
                suppress |= listener.packetError(this, throwable);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
        return suppress;
    }

    @Override
    public void callPacketReceived(Packet packet) {
        try {
            for (int i = 0; i < listeners.length; i++) {
                var listener = listeners[i];
                listener.packetReceived(this, packet);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public void callPacketSent(Packet packet) {
        try {
            for (int i = 0; i < listeners.length; i++) {
                var listener = listeners[i];
                listener.packetSent(this, packet);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }

    @Override
    public void setCompressionThreshold(int threshold, final int level, boolean validateDecompression) {
        this.compressionThreshold = threshold;
        if (this.channel != null) {
            if (this.compressionThreshold >= 0) {
                var existingEncoder = (TcpPacketCompressionAndSizeEncoder) this.channel.pipeline().get(TcpPacketCompressionAndSizeEncoder.ID);
                var existingDecoder = (TcpPacketCompressionDecoder) this.channel.pipeline().get(TcpPacketCompressionDecoder.ID);
                if (existingDecoder != null && existingEncoder != null) {
                    return; // we already updated compression threshold on the session field
                }
                var compressor = Natives.compress.get().create(level);
                LOGGER.debug("Initialized compression variant: {}", Natives.compress.getLoadedVariant());
                var encoder = new TcpPacketCompressionAndSizeEncoder(this, compressor);
                var decoder = new TcpPacketCompressionDecoder(this, validateDecompression, compressor);
                this.channel.pipeline().addAfter(TcpPacketSizeEncoder.ID, TcpPacketCompressionAndSizeEncoder.ID, encoder);
                this.channel.pipeline().addAfter(TcpPacketSizeDecoder.ID, TcpPacketCompressionDecoder.ID, decoder);
                this.channel.pipeline().remove(TcpPacketSizeEncoder.ID);
            } else {
                var encoder = this.channel.pipeline().remove(TcpPacketCompressionAndSizeEncoder.ID);
                var decoder = this.channel.pipeline().remove(TcpPacketCompressionDecoder.ID);
                if (encoder != null && decoder != null) {
                    this.channel.pipeline().addAfter(TcpPacketSizeDecoder.ID, TcpPacketSizeEncoder.ID, new TcpPacketSizeEncoder(this));
                }
            }
        }
    }

    @Override
    public void enableEncryption(SecretKey key) {
        if (channel == null) {
            throw new IllegalStateException("Connect the client before initializing encryption!");
        }
        try {
            var factory = Natives.cipher.get();
            LOGGER.debug("Initialized encryption variant: {}", Natives.cipher.getLoadedVariant());
            var decrypt = factory.forDecryption(key);
            var encrypt = factory.forEncryption(key);
            var encoder = new TcpPacketEncryptionEncoder(this, encrypt);
            var decoder = new TcpPacketEncryptionDecoder(this, decrypt);
            this.channel.pipeline().addBefore(TcpPacketSizeDecoder.ID, TcpPacketEncryptionDecoder.ID, decoder);
            this.channel.pipeline().addBefore(TcpPacketSizeEncoder.ID, TcpPacketEncryptionEncoder.ID, encoder);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize encryption.", e);
        }
    }

    @Override
    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    @Override
    public int getReadTimeout() {
        return this.readTimeout;
    }

    @Override
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
        this.refreshReadTimeoutHandler();
    }

    @Override
    public int getWriteTimeout() {
        return this.writeTimeout;
    }

    @Override
    public void setWriteTimeout(int timeout) {
        this.writeTimeout = timeout;
        this.refreshWriteTimeoutHandler();
    }

    @Override
    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen() && !this.disconnected;
    }

    @Override
    public Future<Void> send(@NotNull Packet packet) {
        if(this.channel == null || !this.channel.isActive()) {
            return Futures.immediateVoidFuture();
        }
        final Packet toSend = this.callPacketSending(packet);
        if (toSend != null) {
            return this.channel.writeAndFlush(toSend).addListener((ChannelFutureListener) future -> {
                if(future.isSuccess()) {
                    callPacketSent(toSend);
                } else {
                    exceptionCaught(null, future.cause());
                }
            });
        }
        return this.channel.newSucceededFuture();
    }

    @Override
    public void send(@NonNull Packet packet, @NonNull ChannelFutureListener listener) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        final Packet toSend = this.callPacketSending(packet);
        if (toSend != null) {
            this.channel.writeAndFlush(toSend).addListener((ChannelFutureListener) future -> {
                if(future.isSuccess()) {
                    callPacketSent(toSend);
                } else {
                    exceptionCaught(null, future.cause());
                }
            }).addListener(listener);
        }
    }

    @Override
    public Future<Void> sendDirect(@NotNull Packet packet) {
        if(this.channel == null || !this.channel.isActive()) {
            return Futures.immediateVoidFuture();
        }
        return this.channel.writeAndFlush(packet).addListener((ChannelFutureListener) future -> {
            if(!future.isSuccess()) {
                exceptionCaught(null, future.cause());
            }
        });
    }

    @Override
    public void sendDelayedDirect(@NotNull Packet packet) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.write(packet);
    }

    @Override
    public void flush() {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.flush();
    }

    @Override
    public void sendBundleDirect(@NonNull Packet... packets) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.eventLoop().execute(() -> {
            this.channel.write(new ClientboundDelimiterPacket());
            for (Packet packet : packets) {
                this.channel.write(packet);
            }
            this.channel.write(new ClientboundDelimiterPacket());
            this.channel.flush();
        });
    }

    @Override
    public void sendBundleDirect(@NonNull List<Packet> packets) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.eventLoop().execute(() -> {
            this.channel.write(new ClientboundDelimiterPacket());
            for (Packet packet : packets) {
                this.channel.write(packet);
            }
            this.channel.write(new ClientboundDelimiterPacket());
            this.channel.flush();
        });
    }

    @Override
    public void sendBundle(@NonNull Packet... packets) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.eventLoop().execute(() -> {
            this.channel.write(new ClientboundDelimiterPacket());
            final List<Packet> sentPacketList = new ArrayList<>(packets.length);
            for (Packet packet : packets) {
                final Packet toSend = this.callPacketSending(packet);
                if (toSend != null) {
                    this.channel.write(toSend);
                    sentPacketList.add(toSend);
                }
                if (sentPacketList.size() > 1000) {
                    this.channel.write(new ClientboundDelimiterPacket());
                    this.channel.flush();
                    for (Packet sentPacket : sentPacketList) {
                        callPacketSent(sentPacket);
                    }
                    sentPacketList.clear();
                    this.channel.write(new ClientboundDelimiterPacket());
                }
            }
            this.channel.write(new ClientboundDelimiterPacket());
            this.channel.flush();
            for (Packet packet : sentPacketList) {
                callPacketSent(packet);
            }
        });
    }

    @Override
    public void sendBundle(@NonNull List<Packet> packets) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.eventLoop().execute(() -> {
            this.channel.write(new ClientboundDelimiterPacket());
            final List<Packet> sentPacketList = new ArrayList<>(packets.size());
            for (Packet packet : packets) {
                final Packet toSend = this.callPacketSending(packet);
                if (toSend != null) {
                    this.channel.write(toSend);
                    sentPacketList.add(toSend);
                }
                if (sentPacketList.size() > 1000) {
                    this.channel.write(new ClientboundDelimiterPacket());
                    this.channel.flush();
                    for (Packet sentPacket : sentPacketList) {
                        callPacketSent(sentPacket);
                    }
                    sentPacketList.clear();
                    this.channel.write(new ClientboundDelimiterPacket());
                }
            }
            this.channel.write(new ClientboundDelimiterPacket());
            this.channel.flush();
            for (Packet packet : sentPacketList) {
                callPacketSent(packet);
            }
        });
    }

    @Override
    public void sendAsync(final @NotNull Packet packet) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.eventLoop().execute(() -> send(packet));
    }

    @Override
    public void sendScheduledAsync(@NonNull Packet packet, long delay, TimeUnit unit) {
        if(this.channel == null || !this.channel.isActive()) {
            return;
        }
        this.channel.eventLoop().schedule(() -> send(packet), delay, unit);
    }

    @Override
    public void disconnect(@NonNull Component reason, @Nullable Throwable cause) {
        if (this.disconnected) {
            return;
        }

        this.disconnected = true;

        this.callDisconnecting(reason, cause);
        if (this.channel != null && this.channel.isOpen()) {
            try {
                this.channel.flush().close().await(5, TimeUnit.SECONDS);
            } catch (final Exception e) {
                this.exceptionCaught(null, e);
            }
        }
        this.callDisconnected(reason, cause);
    }

    protected void refreshReadTimeoutHandler() {
        this.refreshReadTimeoutHandler(this.channel);
    }

    protected void refreshReadTimeoutHandler(Channel channel) {
        if (channel != null) {
            if (this.readTimeout <= 0) {
                if (channel.pipeline().get(READ_TIMEOUT_HANDLER_ID) != null) {
                    channel.pipeline().remove(READ_TIMEOUT_HANDLER_ID);
                }
            } else {
                if (channel.pipeline().get(READ_TIMEOUT_HANDLER_ID) == null) {
                    channel.pipeline().addFirst(READ_TIMEOUT_HANDLER_ID, new ReadTimeoutHandler(this.readTimeout));
                } else {
                    channel.pipeline().replace(READ_TIMEOUT_HANDLER_ID, READ_TIMEOUT_HANDLER_ID, new ReadTimeoutHandler(this.readTimeout));
                }
            }
        }
    }

    protected void refreshWriteTimeoutHandler() {
        this.refreshWriteTimeoutHandler(this.channel);
    }


    protected void refreshWriteTimeoutHandler(Channel channel) {
        if (channel != null) {
            if (this.writeTimeout <= 0) {
                if (channel.pipeline().get(WRITE_TIMEOUT_HANDLER_ID) != null) {
                    channel.pipeline().remove(WRITE_TIMEOUT_HANDLER_ID);
                }
            } else {
                if (channel.pipeline().get(WRITE_TIMEOUT_HANDLER_ID) == null) {
                    channel.pipeline().addFirst(WRITE_TIMEOUT_HANDLER_ID, new WriteTimeoutHandler(this.writeTimeout));
                } else {
                    channel.pipeline().replace(WRITE_TIMEOUT_HANDLER_ID, WRITE_TIMEOUT_HANDLER_ID, new WriteTimeoutHandler(this.writeTimeout));
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (this.disconnected || this.channel != null) {
            ctx.channel().close();
            return;
        }
        this.channel = ctx.channel();
        this.callConnected();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel() == this.channel) {
            this.disconnect("Connection closed.");
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String message;
        if (cause instanceof ConnectTimeoutException || (cause instanceof ConnectException && cause.getMessage().contains("connection timed out"))) {
            message = "Connection timed out.";
        } else if (cause instanceof ReadTimeoutException) {
            message = "Read timed out.";
        } else if (cause instanceof WriteTimeoutException) {
            message = "Write timed out.";
        } else {
            message = cause.toString();
        }

        this.disconnect(message, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        this.callPacketReceived(packet);
        // add below if we want to add more handlers behind this to the netty pipeline
//        ctx.fireChannelRead(packet);
    }
}
