package org.geysermc.mcprotocollib.protocol;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.service.SessionService;
import org.geysermc.mcprotocollib.network.Flag;
import org.geysermc.mcprotocollib.network.packet.DefaultPacketHeader;
import org.geysermc.mcprotocollib.network.packet.PacketHeader;
import org.geysermc.mcprotocollib.network.tcp.TcpClientChannelInitializer;
import org.geysermc.mcprotocollib.network.tcp.TcpServerChannelInitializer;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoBuilder;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoHandler;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerPingTimeHandler;

/**
 * Class containing various constants for Minecraft sessions.
 */
public final class MinecraftConstants {
    // General Key Constants

    /**
     * Session flag where the user {@link GameProfile} is stored.
     */
    public static final Flag<GameProfile> PROFILE_KEY = new Flag<>("profile", GameProfile.class);

    /**
     * Session flag for providing a custom {@link SessionService} instance.
     */
    public static final Flag<SessionService> SESSION_SERVICE_KEY = new Flag<>("session-service", SessionService.class);

    /**
     * Session flag for whether to automatically send a keep alive packet. <br>
     * For servers: determines if the keep alive task is run. <br>
     * For clients: determines if MCProtocolLib automatically sends back a packet
     * when the server sends their keep alive packet.
     */
    public static final Flag<Boolean> AUTOMATIC_KEEP_ALIVE_MANAGEMENT = new Flag<>("manage-keep-alive", Boolean.class);

    // Client Key Constants

    /**
     * Session flag where the user's access token is stored. Client only.
     */
    public static final Flag<String> ACCESS_TOKEN_KEY = new Flag<>("access-token", String.class);

    /**
     * Session flag for providing a custom server info response handler. Client only.
     */
    public static final Flag<ServerInfoHandler> SERVER_INFO_HANDLER_KEY = new Flag<>("server-info-handler", ServerInfoHandler.class);

    /**
     * Session flag for providing a custom ping time response handler. Client only.
     */
    public static final Flag<ServerPingTimeHandler> SERVER_PING_TIME_HANDLER_KEY = new Flag<>("server-ping-time-handler", ServerPingTimeHandler.class);

    // Server Key Constants

    /**
     * Session flag for determining whether to verify users. Server only.
     */
    public static final Flag<Boolean> VERIFY_USERS_KEY = new Flag<>("verify-users", Boolean.class);

    /**
     * Session flag for determining whether to accept transferred connections. Server only.
     */
    public static final Flag<Boolean> ACCEPT_TRANSFERS_KEY = new Flag<>("accept-transfers", Boolean.class);

    /**
     * Session flag for providing a custom server info response builder. Server only.
     */
    public static final Flag<ServerInfoBuilder> SERVER_INFO_BUILDER_KEY = new Flag<>("info-builder", ServerInfoBuilder.class);

    /**
     * Session flag for providing a custom server login handler. Server only.
     */
    public static final Flag<ServerLoginHandler> SERVER_LOGIN_HANDLER_KEY = new Flag<>("login-handler", ServerLoginHandler.class);

    /**
     * Session flag for storing the current ping time. Server only.
     */
    public static final Flag<Long> PING_KEY = new Flag<>("ping", Long.class);


    /**
     * Custom TcpServerChannelInitializer impl
     */
    public static final Flag<TcpServerChannelInitializer.Factory> SERVER_CHANNEL_INITIALIZER = new Flag<>("server-channel-initializer", TcpServerChannelInitializer.Factory.class);
    /**
     * Custom TcpClientChannelInitializer impl
     */
    public static final Flag<TcpClientChannelInitializer.Factory> CLIENT_CHANNEL_INITIALIZER = new Flag<>("client-channel-initializer", TcpClientChannelInitializer.Factory.class);

    /**
     * Mandatory provider that must be set to deserialize ClientboundLevelChunkWithLight packets
     */
    public static ChunkSectionCountProvider CHUNK_SECTION_COUNT_PROVIDER = null;

    /**
     * Session flag for determining the packet compression threshold. Server only.
     */
    public static final Flag<Integer> SERVER_COMPRESSION_THRESHOLD = new Flag<>("compression-threshold", Integer.class);

    /**
     * The packet header used by Minecraft.
     */
    public static final PacketHeader PACKET_HEADER = new DefaultPacketHeader();

    /**
     * The SRV Record prefix used by Minecraft.
     */
    public static final String SRV_RECORD_PREFIX = "_minecraft";
    public static final String SERVER_CLOSING_MESSAGE = "Server closed.";


    private MinecraftConstants() {
    }
}
