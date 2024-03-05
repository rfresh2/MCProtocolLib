package com.github.steveice10.packetlib;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.codec.PacketCodecHelper;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;
import io.netty.channel.ChannelFutureListener;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A network session.
 */
public interface Session {

    /**
     * Connects this session to its host and port.
     */
    public void connect();

    /**
     * Connects this session to its host and port.
     *
     * @param wait Whether to wait for the connection to be established before returning.
     */
    public void connect(boolean wait);

    /**
     * Gets the host the session is connected to.
     *
     * @return The connected host.
     */
    public String getHost();

    /**
     * Gets the port the session is connected to.
     *
     * @return The connected port.
     */
    public int getPort();

    /**
     * Gets the local address of the session.
     *
     * @return The local address, or null if the session is not connected.
     */
    public SocketAddress getLocalAddress();

    /**
     * Gets the remote address of the session.
     *
     * @return The remote address, or null if the session is not connected.
     */
    public SocketAddress getRemoteAddress();

    /**
     * Gets the packet protocol of the session.
     *
     * @return The session's packet protocol.
     */
    public MinecraftProtocol getPacketProtocol();

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
    public Map<String, Object> getFlags();

    /**
     * Checks whether this session has a flag set. If this session belongs to a server,
     * the server's flags will also be checked.
     *
     * @param key Key of the flag to check for.
     * @return Whether this session has a flag set.
     */
    public boolean hasFlag(String key);

    /**
     * Gets the value of the given flag as an instance of the given type. If this
     * session belongs to a server, the server's flags will be checked for the flag
     * as well.
     *
     * @param <T> Type of the flag.
     * @param key Key of the flag.
     * @return Value of the flag.
     * @throws IllegalStateException If the flag's value isn't of the required type.
     */
    public <T> T getFlag(String key);

    /**
     * Gets the value of the given flag as an instance of the given type. If this
     * session belongs to a server, the server's flags will be checked for the flag
     * as well. If the flag is not set, the specified default value will be returned.
     *
     * @param <T> Type of the flag.
     * @param key Key of the flag.
     * @param def Default value of the flag.
     * @return Value of the flag.
     * @throws IllegalStateException If the flag's value isn't of the required type.
     */
    public <T> T getFlag(String key, T def);

    /**
     * Sets the value of a flag. This does not change a server's flags if this session
     * belongs to a server.
     *
     * @param key   Key of the flag.
     * @param value Value to set the flag to.
     */
    public void setFlag(String key, Object value);

    /**
     * Gets the listeners listening on this session.
     *
     * @return This session's listeners.
     */
    public List<SessionListener> getListeners();

    /**
     * Adds a listener to this session.
     *
     * @param listener Listener to add.
     */
    public void addListener(SessionListener listener);

    /**
     * Removes a listener from this session.
     *
     * @param listener Listener to remove.
     */
    public void removeListener(SessionListener listener);

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
    public int getConnectTimeout();

    /**
     * Sets the connect timeout for this session in seconds.
     *
     * @param timeout Connect timeout to set.
     */
    public void setConnectTimeout(int timeout);

    /**
     * Gets the read timeout for this session in seconds.
     *
     * @return The session's read timeout.
     */
    public int getReadTimeout();

    /**
     * Sets the read timeout for this session in seconds.
     *
     * @param timeout Read timeout to set.
     */
    public void setReadTimeout(int timeout);

    /**
     * Gets the write timeout for this session in seconds.
     *
     * @return The session's write timeout.
     */
    public int getWriteTimeout();

    /**
     * Sets the write timeout for this session in seconds.
     *
     * @param timeout Write timeout to set.
     */
    public void setWriteTimeout(int timeout);

    /**
     * Returns true if the session is connected.
     *
     * @return True if the session is connected.
     */
    public boolean isConnected();

    /**
     * Sends a packet.
     *
     * @param packet Packet to send.
     */
    void send(@NonNull Packet packet);

    void send(@NonNull Packet packet, @NonNull ChannelFutureListener listener);

    /**
     * Sends a packet without calling listeners
     * @param packet Packet to send
     */
    void sendDirect(@NonNull Packet packet);

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

    /**
     * Sends a packet on the eventloop after a set delay
     */
    void sendScheduledAsync(@NonNull Packet packet, long delay, TimeUnit unit);

    /**
     * Disconnects the session.
     *
     * @param reason Reason for disconnecting.
     */
    void disconnect(@Nullable String reason);

    /**
     * Disconnects the session.
     *
     * @param reason Reason for disconnecting.
     * @param cause  Throwable responsible for disconnecting.
     */
    void disconnect(@Nullable String reason, Throwable cause);

    /**
     * Disconnects the session.
     *
     * @param reason Reason for disconnecting.
     */
    void disconnect(@Nullable Component reason);

    /**
     * Disconnects the session.
     *
     * @param reason Reason for disconnecting.
     * @param cause  Throwable responsible for disconnecting.
     */
    void disconnect(@Nullable Component reason, Throwable cause);
}
