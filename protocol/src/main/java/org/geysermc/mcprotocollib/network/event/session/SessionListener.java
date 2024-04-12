package org.geysermc.mcprotocollib.network.event.session;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;

/**
 * A listener for listening to session events.
 */
public interface SessionListener {
    /**
     * Called when a session receives a packet.
     *
     * @param packet the packet that was just received.
     */
    void packetReceived(Session session, Packet packet);

    /**
     * Called when a session is sending a packet.
     *
     * @param session Session sending the packet.
     * @param packet  Packet being sent.
     * @return Packet to send, or null to cancel sending.
     */
    Packet packetSending(Session session, Packet packet);

    /**
     * Called when a session sends a packet.
     *
     * @param packet Packet just sent.
     */
    void packetSent(Session session, Packet packet);

    /**
     * Called when a session encounters an error while reading or writing packet data.
     *
     * @param session Current session
     * @param throwable Cause of the error.
     * @return Whether the error should be suppressed.
     */
    boolean packetError(Session session, Throwable throwable);

    /**
     * Called when a session connects.
     *
     * @param session Session that connected.
     */
    void connected(Session session);

    /**
     * Called when a session is about to disconnect.
     *
     * @param session Session being disconnected.
     * @param reason  Reason for the session to disconnect.
     * @param cause   Throwable that caused the disconnect, or null if not caused by a throwable.
     */
    void disconnecting(Session session, Component reason, Throwable cause);

    /**
     * Called when a session is disconnected.
     *
     * @param session Session that disconnected.
     * @param reason  Reason for the session to disconnect.
     * @param cause   Throwable that caused the disconnect, or null if not caused by a throwable.
     */
    void disconnected(Session session, Component reason, Throwable cause);
}
