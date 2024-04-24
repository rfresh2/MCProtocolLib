package org.geysermc.mcprotocollib.protocol;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.Server;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.tcp.TcpClientSession;
import org.geysermc.mcprotocollib.network.tcp.TcpConnectionManager;
import org.geysermc.mcprotocollib.network.tcp.TcpServer;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoBuilder;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.geysermc.mcprotocollib.protocol.MinecraftConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public class MinecraftProtocolTest {
    private static final String HOST = "localhost";
    private static final int PORT = 25562;

    private static final ServerStatusInfo SERVER_INFO = new ServerStatusInfo(
            new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()),
            new PlayerInfo(100, 0, new ArrayList<>()),
            Component.text("Hello world!"),
            null,
            false
    );
    private static final ClientboundLoginPacket JOIN_GAME_PACKET = new ClientboundLoginPacket(0, false, new String[]{"minecraft:world"}, 0, 16, 16, false, false, false, new PlayerSpawnInfo(0, "minecraft:world", 100, GameMode.SURVIVAL, GameMode.SURVIVAL, false, false, null, 100), true);
    private static final Supplier<MinecraftProtocol> PROTOCOL_PROVIDER = () -> {
        var p = new MinecraftProtocol("Username");
        p.setUseDefaultListeners(true);
        return p;
    };

    private static Server server;
    private static TcpConnectionManager connectionManager;

    @BeforeAll
    public static void setupServer() {
        connectionManager = new TcpConnectionManager();
        server = new TcpServer(HOST, PORT, PROTOCOL_PROVIDER, connectionManager);
        server.setGlobalFlag(VERIFY_USERS_KEY, false);
        server.setGlobalFlag(SERVER_COMPRESSION_THRESHOLD, 100);
        server.setGlobalFlag(SERVER_INFO_BUILDER_KEY, (ServerInfoBuilder) session -> SERVER_INFO);
        server.setGlobalFlag(SERVER_LOGIN_HANDLER_KEY, (ServerLoginHandler) session -> {
            // Seems like in this setup the server can reply too quickly to ServerboundFinishConfigurationPacket
            // before the client can transition CONFIGURATION -> GAME. There is probably something wrong here and this is just a band-aid.
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Failed to wait to send ClientboundLoginPacket: " + e.getMessage());
            }
            session.send(JOIN_GAME_PACKET);
        });

        assertTrue(server.bind(true).isListening(), "Could not bind server.");
    }

    @AfterAll
    public static void tearDownServer() {
        if (server != null) {
            server.close(true);
            server = null;
        }
        connectionManager.close();
    }

//    @Test
    public void testStatus() throws InterruptedException {
        MinecraftProtocol protocol = PROTOCOL_PROVIDER.get();
        protocol.setTargetState(ProtocolState.STATUS);
        Session session = new TcpClientSession(HOST, PORT, protocol, connectionManager);
        try {
            ServerInfoHandlerTest handler = new ServerInfoHandlerTest();
            session.setFlag(SERVER_INFO_HANDLER_KEY, handler);
            session.addListener(new DisconnectListener());
            session.connect();

            handler.status.await(4, SECONDS);
            assertNotNull(handler.info, "Failed to get server info.");
            assertEquals(SERVER_INFO, handler.info, "Received incorrect server info.");
        } finally {
            session.disconnect("Status test complete.");
        }
    }

//    @Test
    public void testLogin() throws InterruptedException {
        Session session = new TcpClientSession(HOST, PORT, PROTOCOL_PROVIDER.get(), connectionManager);
        try {
            LoginListenerTest listener = new LoginListenerTest();
            session.addListener(listener);
            session.addListener(new DisconnectListener());
            session.connect(true);

            listener.login.await(4, SECONDS);
            assertNotNull(listener.packet, "Failed to log in.");
            assertEquals(JOIN_GAME_PACKET, listener.packet, "Received incorrect join packet.");
        } finally {
            session.disconnect("Login test complete.");
        }
    }

    private static class ServerInfoHandlerTest implements ServerInfoHandler {
        public final CountDownLatch status = new CountDownLatch(1);
        public ServerStatusInfo info;

        @Override
        public void handle(Session session, ServerStatusInfo info) {
            this.info = info;
            this.status.countDown();
        }
    }

    private static class LoginListenerTest extends SessionAdapter {
        public final CountDownLatch login = new CountDownLatch(1);
        public ClientboundLoginPacket packet;

        @Override
        public void packetReceived(Session session, Packet packet) {
            if (packet instanceof ClientboundLoginPacket) {
                this.packet = (ClientboundLoginPacket) packet;
                this.login.countDown();
            }
        }
    }

    private static class DisconnectListener extends SessionAdapter {
        @Override
        public void disconnected(Session session, Component reason, Throwable cause) {
            System.err.println("Disconnected: " + reason);
            if (cause != null) {
                cause.printStackTrace();
            }
        }
    }
}
