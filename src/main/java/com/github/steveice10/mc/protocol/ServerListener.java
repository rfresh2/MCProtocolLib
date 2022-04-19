package com.github.steveice10.mc.protocol;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.data.status.PlayerInfo;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.VersionInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerKeepAlivePacket;
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
import com.github.steveice10.packetlib.event.session.DisconnectingEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;

import javax.crypto.SecretKey;
import java.net.Proxy;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class ServerListener extends SessionAdapter {
    private static final int DEFAULT_COMPRESSION_THRESHOLD = 256;

    // Always empty post-1.7
    private static final String SERVER_ID = "";
    private static final KeyPair KEY_PAIR;

    static {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            KEY_PAIR = gen.generateKeyPair();
        } catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate server key pair.", e);
        }
    }

    private byte[] verifyToken = new byte[4];
    private String username = "";

    private long lastPingTime = 0;
    private int lastPingId = 0;

    public ServerListener() {
        new Random().nextBytes(this.verifyToken);
    }

    @Override
    public void connected(ConnectedEvent event) {
        event.getSession().setFlag(MinecraftConstants.PING_KEY, 0);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        MinecraftProtocol protocol = (MinecraftProtocol) session.getPacketProtocol();
        if(protocol.getSubProtocol() == SubProtocol.HANDSHAKE) {
            if(packet instanceof HandshakePacket) {
                HandshakePacket handshakePacket = (HandshakePacket) packet;
                switch(handshakePacket.getIntent()) {
                    case STATUS:
                        protocol.setSubProtocol(SubProtocol.STATUS);
                        break;
                    case LOGIN:
                        protocol.setSubProtocol(SubProtocol.LOGIN);
                        if(handshakePacket.getProtocolVersion() > MinecraftConstants.PROTOCOL_VERSION) {
                            session.disconnect("Outdated server! I'm still on " + MinecraftConstants.GAME_VERSION + ".");
                        } else if(handshakePacket.getProtocolVersion() < MinecraftConstants.PROTOCOL_VERSION) {
                            session.disconnect("Outdated client! Please use " + MinecraftConstants.GAME_VERSION + ".");
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Invalid client intent: " + handshakePacket.getIntent());
                }
            }
        }

        if(protocol.getSubProtocol() == SubProtocol.LOGIN) {
            if(packet instanceof LoginStartPacket) {
                final LoginStartPacket loginStartPacket = (LoginStartPacket) packet;
                this.username = loginStartPacket.getUsername();

                if(session.getFlag(MinecraftConstants.VERIFY_USERS_KEY, true)) {
                    session.send(new EncryptionRequestPacket(SERVER_ID, KEY_PAIR.getPublic(), this.verifyToken));
                } else {
                    new Thread(new UserAuthTask(session, null)).start();
                }
            } else if(packet instanceof EncryptionResponsePacket) {
                EncryptionResponsePacket encryptionResponsePacket = (EncryptionResponsePacket) packet;
                PrivateKey privateKey = KEY_PAIR.getPrivate();
                if(!Arrays.equals(this.verifyToken, encryptionResponsePacket.getVerifyToken(privateKey))) {
                    session.disconnect("Invalid nonce!");
                    return;
                }

                SecretKey key = encryptionResponsePacket.getSecretKey(privateKey);
                session.enableEncryption(protocol.enableEncryption(key));
                new Thread(new UserAuthTask(session, key)).start();
            }
        }

        if(protocol.getSubProtocol() == SubProtocol.STATUS) {
            if(packet instanceof StatusQueryPacket) {
                ServerInfoBuilder builder = session.getFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY);
                if(builder == null) {
                    builder = session1 -> new ServerStatusInfo(
                            VersionInfo.CURRENT,
                            new PlayerInfo(0, 20, new GameProfile[0]),
                            Message.fromString("A Minecraft Server"),
                            null
                    );
                }

                ServerStatusInfo info = builder.buildInfo(session);
                session.send(new StatusResponsePacket(info));
            } else if(packet instanceof StatusPingPacket) {
                session.send(new StatusPongPacket(((StatusPingPacket) packet).getPingTime()));
            }
        }

        if(protocol.getSubProtocol() == SubProtocol.GAME) {
            if(packet instanceof ClientKeepAlivePacket) {
                ClientKeepAlivePacket clientKeepAlivePacket = (ClientKeepAlivePacket) packet;
                if(clientKeepAlivePacket.getPingId() == this.lastPingId) {
                    long time = System.currentTimeMillis() - this.lastPingTime;
                    session.setFlag(MinecraftConstants.PING_KEY, time);
                }
            }
        }
    }

    @Override
    public void packetSent(Session session, Packet packet) {
        if(packet instanceof LoginSetCompressionPacket) {
            session.setCompressionThreshold(((LoginSetCompressionPacket) packet).getThreshold(), false);
            session.send(new LoginSuccessPacket((GameProfile) session.getFlag(MinecraftConstants.PROFILE_KEY)));
        } else if(packet instanceof LoginSuccessPacket) {
            ((MinecraftProtocol) session.getPacketProtocol()).setSubProtocol(SubProtocol.GAME);
            ServerLoginHandler handler = session.getFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY);
            if(handler != null) {
                handler.loggedIn(session);
            }

            new Thread(new KeepAliveTask(session)).start();
        }
    }

    @Override
    public void disconnecting(DisconnectingEvent event) {
        MinecraftProtocol protocol = (MinecraftProtocol) event.getSession().getPacketProtocol();
        if(protocol.getSubProtocol() == SubProtocol.LOGIN) {
            event.getSession().send(new LoginDisconnectPacket(event.getReason(), true));
        } else if(protocol.getSubProtocol() == SubProtocol.GAME) {
            event.getSession().send(new ServerDisconnectPacket(event.getReason(), true));
        }
    }

    private class UserAuthTask implements Runnable {
        private Session session;
        private SecretKey key;

        public UserAuthTask(Session session, SecretKey key) {
            this.key = key;
            this.session = session;
        }

        @Override
        public void run() {
            GameProfile profile = null;
            if(this.key != null) {
                SessionService sessionService = new SessionService();
                try {
                    profile = sessionService.getProfileByServer(username, sessionService.getServerId(SERVER_ID, KEY_PAIR.getPublic(), this.key));
                } catch(RequestException e) {
                    this.session.disconnect("Failed to make session service request.", e);
                    return;
                }

                if(profile == null) {
                    this.session.disconnect("Failed to verify username.");
                }
            } else {
                profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()), username);
            }

            this.session.setFlag(MinecraftConstants.PROFILE_KEY, profile);

            int threshold = session.getFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, DEFAULT_COMPRESSION_THRESHOLD);
            this.session.send(new LoginSetCompressionPacket(threshold));
        }
    }

    private class KeepAliveTask implements Runnable {
        private Session session;

        public KeepAliveTask(Session session) {
            this.session = session;
        }

        @Override
        public void run() {
            while(this.session.isConnected()) {
                lastPingTime = System.currentTimeMillis();
                lastPingId = (int) lastPingTime;
                this.session.send(new ServerKeepAlivePacket(lastPingId));

                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e) {
                    break;
                }
            }
        }
    }
}
