package com.github.steveice10.packetlib.event.session;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import net.kyori.adventure.text.Component;

/**
 * An adapter for picking session events to listen for.
 */
public class SessionAdapter implements SessionListener {
    @Override
    public void packetReceived(Session session, Packet packet) { }

    @Override
    public Packet packetSending(Session session, Packet packet) {
        return packet;
    }

    @Override
    public void packetSent(Session session, Packet packet) { }

    @Override
    public boolean packetError(Session session, Throwable cause) {
        return false;
    }

    @Override
    public void connected(Session session) { }

    @Override
    public void disconnecting(Session session, Component reason, Throwable cause) { }

    @Override
    public void disconnected(Session session, Component reason, Throwable cause) { }
}
