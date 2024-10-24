package org.geysermc.mcprotocollib.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.network.codec.PacketCodecHelper;
import org.geysermc.mcprotocollib.network.event.session.SessionListener;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.tcp.FlushHandler;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;

import javax.crypto.SecretKey;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A network session.
 */
public interface Session {

    /**
     * Connects this session to its host and port.
     */
    void connect();

    /**
     * Connects this session to its host and port.
     *
     * @param wait Whether to wait for the connection to be established before returning.
     */
    void connect(boolean wait);

    /**
     * Connects this session to its host and port.
     *
     * @param wait Whether to wait for the connection to be established before returning.
     * @param transferring Whether the session is a client being transferred.
     */
    void connect(boolean wait, boolean transferring);

    /**
     * Gets the host the session is connected to.
     *
     * @return The connected host.
     */
    String getHost();

    /**
     * Gets the port the session is connected to.
     *
     * @return The connected port.
     */
    int getPort();

    /**
     * Gets the local address of the session.
     *
     * @return The local address, or null if the session is not connected.
     */
    SocketAddress getLocalAddress();

    /**
     * Gets the remote address of the session.
     *
     * @return The remote address, or null if the session is not connected.
     */
    SocketAddress getRemoteAddress();

    /**
     * Gets the packet protocol of the session.
     *
     * @return The session's packet protocol.
     */
    MinecraftProtocol getPacketProtocol();

    /**
     * Gets the session's {@link PacketCodecHelper}.
     *
     * @return The session's packet codec helper.
     */
    PacketCodecHelper getCodecHelper();

    /**
     * Gets this session's set flags. If this session belongs to a server, the server's
     * flags will be included in the results.
     *
     * @return This session's flags.
     */
    Map<String, Object> getFlags();

    /**
     * Checks whether this session has a flag set. If this session belongs to a server,
     * the server's flags will also be checked.
     *
     * @param flag Flag to check for.
     * @return Whether this session has a flag set.
     */
    boolean hasFlag(Flag<?> flag);

    /**
     * Gets the value of the given flag as an instance of the given type. If this
     * session belongs to a server, the server's flags will be checked for the flag
     * as well.
     *
     * @param <T> Type of the flag.
     * @param flag Flag to check for.
     * @return Value of the flag.
     * @throws IllegalStateException If the flag's value isn't of the required type.
     */
    <T> T getFlag(Flag<T> flag);

    /**
     * Gets the value of the given flag as an instance of the given type. If this
     * session belongs to a server, the server's flags will be checked for the flag
     * as well. If the flag is not set, the specified default value will be returned.
     *
     * @param <T> Type of the flag.
     * @param flag Flag to check for.
     * @param def Default value of the flag.
     * @return Value of the flag.
     * @throws IllegalStateException If the flag's value isn't of the required type.
     */
    <T> T getFlag(Flag<T> flag, T def);

    /**
     * Sets the value of a flag. This does not change a server's flags if this session
     * belongs to a server.
     *
     * @param <T> Type of the flag.
     * @param flag Flag to check for.
     * @param value Value to set the flag to.
     */
    <T> void setFlag(Flag<T> flag, T value);

    /**
     * Sets the values for a collection of flags.
     *
     * @param flags Collection of flags
     */
    void setFlags(Map<String, Object> flags);

    /**
     * Gets the listeners listening on this session.
     *
     * @return This session's listeners.
     */
    List<SessionListener> getListeners();

    /**
     * Adds a listener to this session.
     *
     * @param listener Listener to add.
     */
    void addListener(SessionListener listener);

    /**
     * Removes a listener from this session.
     *
     * @param listener Listener to remove.
     */
    void removeListener(SessionListener listener);

    /**
     * Notifies all listeners that a packet was just received.
     *
     * @param packet Packet to notify.
     */
    void callPacketReceived(Packet packet);

    Packet callPacketSending(Packet packet);

    void callConnected();

    void callDisconnecting(Component reason, Throwable cause);

    void callDisconnected(Component reason, Throwable cause);

    /**
     * Notifies all listeners that a packet was just sent.
     *
     * @param packet Packet to notify.
     */
    void callPacketSent(Packet packet);

    boolean callPacketError(Throwable throwable);

    /**
     * Gets the compression packet length threshold for this session (-1 = disabled).
     *
     * @return This session's compression threshold.
     */
    int getCompressionThreshold();

    /**
     * Sets the compression packet length threshold for this session (-1 = disabled).
     *
     * @param threshold             The new compression threshold.
     * @param level                 Higher level = more compression, but more CPU. -1 = default level
     * @param validateDecompression whether to validate that the decompression fits within size checks.
     */
    void setCompressionThreshold(int threshold, final int level, boolean validateDecompression);

    /**
     * Enables encryption for this session.
     *
     * @param key the secret key to encrypt with
     */
    void enableEncryption(SecretKey key);

    /**
     * Gets the connect timeout for this session in seconds.
     *
     * @return The session's connect timeout.
     */
    int getConnectTimeout();

    /**
     * Sets the connect timeout for this session in seconds.
     *
     * @param timeout Connect timeout to set.
     */
    void setConnectTimeout(int timeout);

    /**
     * Gets the read timeout for this session in seconds.
     *
     * @return The session's read timeout.
     */
    int getReadTimeout();

    /**
     * Sets the read timeout for this session in seconds.
     *
     * @param timeout Read timeout to set.
     */
    void setReadTimeout(int timeout);

    /**
     * Gets the write timeout for this session in seconds.
     *
     * @return The session's write timeout.
     */
    int getWriteTimeout();

    /**
     * Sets the write timeout for this session in seconds.
     *
     * @param timeout Write timeout to set.
     */
    void setWriteTimeout(int timeout);

    /**
     * Returns true if the session is connected.
     *
     * @return True if the session is connected.
     */
    boolean isConnected();

    /**
     * Sends a packet.
     *
     * @param packet Packet to send.
     */
    Future<Void> send(@NonNull Packet packet);

    void send(@NonNull Packet packet, @NonNull ChannelFutureListener listener);

    /**
     * Sends a packet without calling listeners
     *
     * @param packet Packet to send
     * @return
     */
    Future<Void> sendDirect(@NonNull Packet packet);

    /**
     * Writes a packet without flushing
     * @param packet Packet to send
     */
    void sendDelayedDirect(@NonNull Packet packet);

    /**
     * Flushes all pending packets.
     */
    void flush();

    void sendBundleDirect(@NonNull Packet... packets);
    void sendBundleDirect(@NonNull List<Packet> packets);
    void sendBundle(@NonNull List<Packet> packets);
    void sendBundle(@NonNull Packet... packets);

    /**
     * Sends a packet on the event loop
     */
    void sendAsync(@NonNull Packet packet);

    void sendScheduledAsync(@NonNull Packet packet, long delay, TimeUnit unit);

    /**
     * Disconnects the session.
     * This method just wraps the reason into a {@link Component}.
     * It is recommended to use Components instead as they provide more flexibility.
     *
     * @param reason Reason for disconnecting.
     * @see #disconnect(String, Throwable)
     */
    default void disconnect(@NonNull String reason) {
        this.disconnect(reason, null);
    }

    /**
     * Disconnects the session.
     * This method just wraps the reason into a {@link Component}.
     * It is recommended to use Components instead as they provide more flexibility.
     *
     * @param reason Reason for disconnecting.
     * @param cause Throwable responsible for disconnecting.
     * @see #disconnect(Component, Throwable)
     */
    default void disconnect(@NonNull String reason, @Nullable Throwable cause) {
        this.disconnect(Component.text(reason), cause);
    }

    /**
     * Disconnects the session.
     *
     * @param reason Reason for disconnecting.
     */
    default void disconnect(@NonNull Component reason) {
        this.disconnect(reason, null);
    }

    /**
     * Disconnects the session.
     *
     * @param reason Reason for disconnecting.
     * @param cause  Throwable responsible for disconnecting.
     */
    void disconnect(@NonNull Component reason, @Nullable Throwable cause);

    /**
     * Auto read in netty means that the server is automatically reading from the channel.
     * Turning it off means that we won't get more packets being decoded until we turn it back on.
     * We use this to hold off on reading packets until we are ready to process them.
     * For example this is used for switching inbound states with {@link #switchInboundState(ProtocolState)}.
     *
     * @param autoRead Whether to enable auto read.
     *                 Default is true.
     */
    void setAutoRead(boolean autoRead);

    /**
     * Returns the underlying netty channel of this session.
     *
     * @return The netty channel
     */
    Channel getChannel();

    /**
     * Changes the inbound state of the session and then re-enables auto read.
     * This is used after a terminal packet was handled and the session is ready to receive more packets in the new state.
     *
     */
    default void switchInboundState(ProtocolState protocolState) {
        getPacketProtocol().setInboundState(protocolState);
        // We switched to the new inbound state
        // we can start reading again
        setAutoRead(true);
    }

    /**
     * Flushes all packets that are due to be sent and changes the outbound state of the session.
     * This makes sure no other threads have scheduled packets to be sent.
     */
    default void switchOutboundState(ProtocolState protocolState) {
        if (getChannel().eventLoop().inEventLoop()) {
            getChannel().writeAndFlush(FlushHandler.FLUSH_PACKET).addListener(f -> {
                getPacketProtocol().setOutboundState(protocolState);
            });
        } else {
            getChannel().writeAndFlush(FlushHandler.FLUSH_PACKET).syncUninterruptibly();
            getPacketProtocol().setOutboundState(protocolState);
        }
    }
}
