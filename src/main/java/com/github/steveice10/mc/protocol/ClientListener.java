package com.github.steveice10.mc.protocol;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.exception.request.InvalidCredentialsException;
import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.exception.request.ServiceUnavailableException;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.handshake.HandshakeIntent;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.mc.protocol.data.status.handler.ServerPingTimeHandler;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerSetCompressionPacket;
import com.github.steveice10.mc.protocol.packet.login.client.EncryptionResponsePacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.mc.protocol.packet.login.server.EncryptionRequestPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSetCompressionPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.mc.protocol.packet.status.client.StatusPingPacket;
import com.github.steveice10.mc.protocol.packet.status.client.StatusQueryPacket;
import com.github.steveice10.mc.protocol.packet.status.server.StatusPongPacket;
import com.github.steveice10.mc.protocol.packet.status.server.StatusResponsePacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.net.Proxy;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
public class ClientListener extends SessionAdapter {
    private final @NonNull SubProtocol targetSubProtocol;

    @Override
    public void packetReceived(Session session, Packet packet) {
        MinecraftProtocol protocol = (MinecraftProtocol) session.getPacketProtocol();
        if(protocol.getSubProtocol() == SubProtocol.LOGIN) {
            if(packet instanceof EncryptionRequestPacket) {
                EncryptionRequestPacket encryptionRequestPacket = (EncryptionRequestPacket) packet;
                SecretKey key;
                try {
                    KeyGenerator gen = KeyGenerator.getInstance("AES");
                    gen.init(128);
                    key = gen.generateKey();
                } catch(NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Failed to generate shared key.", e);
                }

                SessionService sessionService = new SessionService();
                GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
                String serverId = sessionService.getServerId(encryptionRequestPacket.getServerId(), encryptionRequestPacket.getPublicKey(), key);
                String accessToken = session.getFlag(MinecraftConstants.ACCESS_TOKEN_KEY);
                try {
                    sessionService.joinServer(profile, accessToken, serverId);
                } catch(ServiceUnavailableException e) {
                    session.disconnect("Login failed: Authentication service unavailable.", e);
                    return;
                } catch(InvalidCredentialsException e) {
                    session.disconnect("Login failed: Invalid login session.", e);
                    return;
                } catch(RequestException e) {
                    session.disconnect("Login failed: Authentication error: " + e.getMessage(), e);
                    return;
                }

                session.send(new EncryptionResponsePacket(key, ((EncryptionRequestPacket) packet).getPublicKey(), ((EncryptionRequestPacket) packet).getVerifyToken()));
                session.enableEncryption(protocol.enableEncryption(key));
            } else if(packet instanceof LoginSuccessPacket) {
                LoginSuccessPacket loginSuccessPacket = (LoginSuccessPacket) packet;
                session.setFlag(MinecraftConstants.PROFILE_KEY, loginSuccessPacket.getProfile());
                protocol.setSubProtocol(SubProtocol.GAME);
            } else if(packet instanceof LoginDisconnectPacket) {
                LoginDisconnectPacket loginDisconnectPacket = (LoginDisconnectPacket) packet;
                session.disconnect(loginDisconnectPacket.getReason());
            } else if(packet instanceof LoginSetCompressionPacket) {
                session.setCompressionThreshold(((LoginSetCompressionPacket) packet).getThreshold(), false);
            }
        } else if(protocol.getSubProtocol() == SubProtocol.STATUS) {
            if(packet instanceof StatusResponsePacket) {
                ServerStatusInfo info = ((StatusResponsePacket) packet).getInfo();
                ServerInfoHandler handler = session.getFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY);
                if(handler != null) {
                    handler.handle(session, info);
                }

                session.send(new StatusPingPacket(System.currentTimeMillis()));
            } else if(packet instanceof StatusPongPacket) {
                long time = System.currentTimeMillis() - ((StatusPongPacket) packet).getPingTime();
                ServerPingTimeHandler handler = session.getFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY);
                if(handler != null) {
                    handler.handle(session, time);
                }

                session.disconnect("Finished");
            }
        } else if(protocol.getSubProtocol() == SubProtocol.GAME) {
            if(packet instanceof ServerKeepAlivePacket) {
                session.send(new ClientKeepAlivePacket(((ServerKeepAlivePacket) packet).getPingId()));
            } else if(packet instanceof ServerDisconnectPacket) {
                session.disconnect(((ServerDisconnectPacket) packet).getReason());
            } else if(packet instanceof ServerSetCompressionPacket) {
                session.setCompressionThreshold(((ServerSetCompressionPacket) packet).getThreshold(), false);
            }
        }
    }

    @Override
    public void packetSent(Session session, Packet packet) {
        if(packet instanceof HandshakePacket) {
            // Once the HandshakePacket has been sent, switch to the next protocol mode.
            MinecraftProtocol protocol = (MinecraftProtocol) session.getPacketProtocol();
            protocol.setSubProtocol(this.targetSubProtocol);

            if(this.targetSubProtocol == SubProtocol.LOGIN) {
                GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
                session.send(new LoginStartPacket(profile != null ? profile.getName() : ""));
            } else {
                session.send(new StatusQueryPacket());
            }
        }
    }

    @Override
    public void connected(ConnectedEvent event) {
        if(this.targetSubProtocol == SubProtocol.LOGIN) {
            event.getSession().send(new HandshakePacket(MinecraftConstants.PROTOCOL_VERSION, event.getSession().getHost(), event.getSession().getPort(), HandshakeIntent.LOGIN));
        } else if(this.targetSubProtocol == SubProtocol.STATUS) {
            event.getSession().send(new HandshakePacket(MinecraftConstants.PROTOCOL_VERSION, event.getSession().getHost(), event.getSession().getPort(), HandshakeIntent.STATUS));
        }
    }
}
