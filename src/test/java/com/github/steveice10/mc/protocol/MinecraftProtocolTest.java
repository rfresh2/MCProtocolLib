package com.github.steveice10.mc.protocol;

import com.github.steveice10.mc.protocol.codec.MinecraftCodec;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.status.PlayerInfo;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.VersionInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.io.NBTIO;
import com.github.steveice10.opennbt.tag.limiter.TagLimiter;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import com.github.steveice10.packetlib.tcp.TcpServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

import static com.github.steveice10.mc.protocol.MinecraftConstants.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

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
    private static final ClientboundLoginPacket JOIN_GAME_PACKET = new ClientboundLoginPacket(0, false, GameMode.SURVIVAL, GameMode.SURVIVAL, new String[]{"minecraft:world"}, loadLoginRegistry(), "overworld", "minecraft:world", 100, 0, 16, 16, false, false, false, false, null, 100);

    private static Server server;

    @BeforeClass
    public static void setupServer() {
        server = new TcpServer(HOST, PORT, () -> {
            var protocol = new MinecraftProtocol();
            protocol.setUseDefaultListeners(true);
            return protocol;
        });
        server.setGlobalFlag(VERIFY_USERS_KEY, false);
        server.setGlobalFlag(SERVER_COMPRESSION_THRESHOLD, 100);
        server.setGlobalFlag(SERVER_INFO_BUILDER_KEY, (ServerInfoBuilder) session -> SERVER_INFO);
        server.setGlobalFlag(SERVER_LOGIN_HANDLER_KEY, (ServerLoginHandler) session -> session.send(JOIN_GAME_PACKET));

        assertTrue("Could not bind server.", server.bind(true).isListening());
    }

    @AfterClass
    public static void tearDownServer() {
        if (server != null) {
            server.close(true);
            server = null;
        }
    }

    @Test
    public void testStatus() throws InterruptedException {
        var protocol = new MinecraftProtocol();
        protocol.setUseDefaultListeners(true);
        Session session = new TcpClientSession(HOST, PORT, protocol);
        try {
            ServerInfoHandlerTest handler = new ServerInfoHandlerTest();
            session.setFlag(SERVER_INFO_HANDLER_KEY, handler);
            session.addListener(new DisconnectListener());
            session.connect();

            handler.status.await(4, SECONDS);
            assertNotNull("Failed to get server info.", handler.info);
            assertEquals("Received incorrect server info.", SERVER_INFO, handler.info);
        } finally {
            session.disconnect("Status test complete.");
        }
    }

    @Test
    public void testLogin() throws InterruptedException {
        var protocol = new MinecraftProtocol("Username");
        protocol.setUseDefaultListeners(true);
        Session session = new TcpClientSession(HOST, PORT, protocol);
        try {
            LoginListenerTest listener = new LoginListenerTest();
            session.addListener(listener);
            session.addListener(new DisconnectListener());
            session.connect();

            listener.login.await(4, SECONDS);
            assertNotNull("Failed to log in.", listener.packet);
            assertEquals("Received incorrect join packet.", JOIN_GAME_PACKET, listener.packet);
        } finally {
            session.disconnect("Login test complete.");
        }
    }

    private static class ServerInfoHandlerTest implements ServerInfoHandler {
        public CountDownLatch status = new CountDownLatch(1);
        public ServerStatusInfo info;

        @Override
        public void handle(Session session, ServerStatusInfo info) {
            this.info = info;
            this.status.countDown();
        }
    }

    private static class LoginListenerTest extends SessionAdapter {
        public CountDownLatch login = new CountDownLatch(1);
        public ClientboundLoginPacket packet;

        @Override
        public void packetReceived(@NotNull Session session, @NotNull Packet packet) {
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

    public static CompoundTag loadLoginRegistry() {
        try (InputStream inputStream = MinecraftProtocolTest.class.getClassLoader().getResourceAsStream("networkCodec.nbt");
            DataInputStream stream = new DataInputStream(new GZIPInputStream(inputStream))) {
            return NBTIO.readTag(stream, TagLimiter.noop(), true, CompoundTag.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("Unable to load network codec.");
        }
    }
}
