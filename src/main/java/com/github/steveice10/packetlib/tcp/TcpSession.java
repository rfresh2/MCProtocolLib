package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundDelimiterPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;
import com.google.common.util.concurrent.Futures;
import com.velocitypowered.natives.util.Natives;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class TcpSession extends SimpleChannelInboundHandler<Packet> implements Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSession.class);
    protected String host;
    protected int port;
    private final MinecraftProtocol protocol;
    private int compressionThreshold = -1;
    private int connectTimeout = 30;
    private int readTimeout = 30;
    private int writeTimeout = 0;

    private final Map<String, Object> flags = new HashMap<>();
    private final List<SessionListener> listeners = new CopyOnWriteArrayList<>();

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
    public boolean hasFlag(String key) {
        return this.flags.containsKey(key);
    }

    @Override
    public <T> T getFlag(String key) {
        return this.getFlag(key, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFlag(String key, T def) {
        Object value = this.flags.get(key);
        if (value == null) {
            return def;
        }

        try {
            return (T) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Tried to get flag \"" + key + "\" as the wrong type. Actual type: " + value.getClass().getName());
        }
    }

    @Override
    public void setFlag(String key, Object value) {
        this.flags.put(key, value);
    }

    @Override
    public List<SessionListener> getListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    @Override
    public void addListener(SessionListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(SessionListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public Packet callPacketSending(final Packet packet) {
        Packet toSend = packet;
        try {
            for (SessionListener listener : this.listeners) {
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
            for (SessionListener listener : this.listeners) {
                listener.connected(this);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public void callDisconnecting(final Component reason, final Throwable cause) {
        try {
            for (SessionListener listener : this.listeners) {
                listener.disconnecting(this, reason, cause);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public void callDisconnected(final Component reason, final Throwable cause) {
        try {
            for (SessionListener listener : this.listeners) {
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
            for (SessionListener listener : this.listeners) {
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
            for (SessionListener listener : this.listeners) {
                listener.packetReceived(this, packet);
            }
        } catch (Throwable t) {
            exceptionCaught(null, t);
        }
    }

    @Override
    public void callPacketSent(Packet packet) {
        try {
            for (SessionListener listener : this.listeners) {
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
                var existingEncoder = (TcpPacketCompressionAndSizeEncoder) this.channel.pipeline().get("compression-encoder");
                var existingDecoder = (TcpPacketCompressionDecoder) this.channel.pipeline().get("compression-decoder");
                if (existingDecoder != null && existingEncoder != null) {
                    return; // we already updated compression threshold on the session field
                }
                var compressor = Natives.compress.get().create(level);
                var encoder = new TcpPacketCompressionAndSizeEncoder(this, compressor);
                var decoder = new TcpPacketCompressionDecoder(this, validateDecompression, compressor);
                this.channel.pipeline().addAfter("size-encoder", "compression-encoder", encoder);
                this.channel.pipeline().addAfter("size-decoder", "compression-decoder", decoder);
                this.channel.pipeline().remove("size-encoder");
            } else {
                var encoder = this.channel.pipeline().remove("compression-encoder");
                var decoder = this.channel.pipeline().remove("compression-decoder");
                if (encoder != null && decoder != null) {
                    this.channel.pipeline().addAfter("size-decoder", "size-encoder", new TcpPacketSizeEncoder(this));
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
            var decrypt = factory.forDecryption(key);
            var encrypt = factory.forEncryption(key);
            var encoder = new TcpPacketEncryptionEncoder(this, encrypt);
            var decoder = new TcpPacketEncryptionDecoder(decrypt);
            this.channel.pipeline().addBefore("size-decoder", "encryption-decoder", decoder);
            this.channel.pipeline().addBefore("size-encoder", "encryption-encoder", encoder);
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
    public void disconnect(String reason) {
        this.disconnect(Component.text(reason));
    }

    @Override
    public void disconnect(String reason, Throwable cause) {
        this.disconnect(Component.text(reason), cause);
    }

    @Override
    public void disconnect(Component reason) {
        this.disconnect(reason, null);
    }

    @Override
    public void disconnect(final Component reason, final Throwable cause) {
        if (this.disconnected) {
            return;
        }

        this.disconnected = true;

        if (this.channel != null && this.channel.isOpen()) {
            this.callDisconnecting(reason, cause);
            try {
                this.channel.flush().close().await(5, TimeUnit.SECONDS);
            } catch (final Exception e) {
                this.exceptionCaught(null, e);
            }
            this.callDisconnected(reason != null ? reason : Component.text("Connection closed."), cause);
        } else {
            this.callDisconnected(reason != null ? reason : Component.text("Connection closed."), cause);
        }
    }

    public Channel getChannel() {
        return this.channel;
    }

    protected void refreshReadTimeoutHandler() {
        this.refreshReadTimeoutHandler(this.channel);
    }

    protected void refreshReadTimeoutHandler(Channel channel) {
        if (channel != null) {
            if (this.readTimeout <= 0) {
                if (channel.pipeline().get("readTimeout") != null) {
                    channel.pipeline().remove("readTimeout");
                }
            } else {
                if (channel.pipeline().get("readTimeout") == null) {
                    channel.pipeline().addFirst("readTimeout", new ReadTimeoutHandler(this.readTimeout));
                } else {
                    channel.pipeline().replace("readTimeout", "readTimeout", new ReadTimeoutHandler(this.readTimeout));
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
                if (channel.pipeline().get("writeTimeout") != null) {
                    channel.pipeline().remove("writeTimeout");
                }
            } else {
                if (channel.pipeline().get("writeTimeout") == null) {
                    channel.pipeline().addFirst("writeTimeout", new WriteTimeoutHandler(this.writeTimeout));
                } else {
                    channel.pipeline().replace("writeTimeout", "writeTimeout", new WriteTimeoutHandler(this.writeTimeout));
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
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel() == this.channel) {
            this.disconnect("Connection closed.");
        }
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
    }
}
