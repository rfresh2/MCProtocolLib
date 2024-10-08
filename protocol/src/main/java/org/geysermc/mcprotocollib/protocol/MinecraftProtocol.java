package org.geysermc.mcprotocollib.protocol;

import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.CompoundTag;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Server;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.PacketHeader;
import org.geysermc.mcprotocollib.network.packet.PacketProtocol;
import org.geysermc.mcprotocollib.network.packet.PacketRegistry;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.PacketCodec;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/**
 * Implements the Minecraft protocol.
 */
public class MinecraftProtocol extends PacketProtocol {
    private static final Logger log = LoggerFactory.getLogger(MinecraftProtocol.class);

    /**
     * The network codec sent from the server to the client during {@link ProtocolState#CONFIGURATION}.
     * Lazily loaded once when {@link #newServerSession(Server, Session)} is invoked,
     * if {@link #isUseDefaultListeners()} is true.
     */
    @Nullable
    private static CompoundTag DEFAULT_NETWORK_CODEC;

    /**
     * The codec used for the Minecraft protocol.
     */
    @Getter
    private final PacketCodec codec;

    /**
     * -- GETTER --
     *  Gets the current inbound
     *  we're in.
     *
     * @return The current inbound {@link ProtocolState}.
     */
    @Getter
    private ProtocolState inboundState;
    private PacketRegistry inboundStateRegistry;

    /**
     * -- GETTER --
     *  Gets the current outbound
     *  we're in.
     *
     * @return The current outbound {@link ProtocolState}.
     */
    @Getter
    private ProtocolState outboundState;
    private PacketRegistry outboundStateRegistry;

    @Setter
    private ProtocolState targetState;

    /**
     * The player's identity.
     */
    @Getter
    private GameProfile profile;

    /**
     * Authentication access token.
     */
    @Getter
    private String accessToken;

    /**
     * Whether to add the default client and server listeners for performing initial login.
     */
    @Getter
    @Setter
    private boolean useDefaultListeners = false;

    private boolean client = true;

    /**
     * Constructs a new MinecraftProtocol instance for making status queries.
     */
    public MinecraftProtocol() {
        this(MinecraftCodec.CODEC);
    }

    /**
     * Constructs a new MinecraftProtocol instance for making status queries.
     *
     * @param codec The packet codec to use.
     */
    public MinecraftProtocol(PacketCodec codec) {
        this.codec = codec;
        this.targetState = ProtocolState.STATUS;

        resetStates();
    }

    /**
     * Constructs a new MinecraftProtocol instance for logging in using offline mode.
     *
     * @param username Username to use.
     */
    public MinecraftProtocol(@NonNull String username) {
        this(new GameProfile(UUID.randomUUID(), username), null);
    }

    /**
     * Constructs a new MinecraftProtocol instance for logging in using offline mode.
     *
     * @param codec    The packet codec to use.
     * @param username Username to use.
     */
    public MinecraftProtocol(@NonNull PacketCodec codec, @NonNull String username) {
        this(codec, new GameProfile(UUID.randomUUID(), username), null);
    }

    /**
     * Constructs a new MinecraftProtocol instance for logging in.
     *
     * @param profile     GameProfile to use.
     * @param accessToken Access token to use, or null if using offline mode.
     */
    public MinecraftProtocol(@NonNull GameProfile profile, String accessToken) {
        this(MinecraftCodec.CODEC, profile, accessToken);
    }

    /**
     * Constructs a new MinecraftProtocol instance for logging in.
     *
     * @param codec       The packet codec to use.
     * @param profile     GameProfile to use.
     * @param accessToken Access token to use, or null if using offline mode.
     */
    public MinecraftProtocol(@NonNull PacketCodec codec, @NonNull GameProfile profile, String accessToken) {
        this.codec = codec;
        this.targetState = ProtocolState.LOGIN;
        this.profile = profile;
        this.accessToken = accessToken;

        resetStates();
    }

    @Override
    public String getSRVRecordPrefix() {
        return MinecraftConstants.SRV_RECORD_PREFIX;
    }

    @Override
    public PacketHeader getPacketHeader() {
        return MinecraftConstants.PACKET_HEADER;
    }

    @Override
    public MinecraftCodecHelper getHelper() {
        return this.codec.getHelper();
    }

    @Override
    public void newClientSession(Session session, boolean transferring) {
        session.setFlag(MinecraftConstants.PROFILE_KEY, this.profile);
        session.setFlag(MinecraftConstants.ACCESS_TOKEN_KEY, this.accessToken);

        resetStates();

        if (this.useDefaultListeners) {
            session.addListener(new ClientListener(this.targetState, transferring));
        }
        this.client = true;
    }

    @Override
    public void newServerSession(Server server, Session session) {
        resetStates();

        if (this.useDefaultListeners) {
            if (DEFAULT_NETWORK_CODEC == null) {
                DEFAULT_NETWORK_CODEC = loadNetworkCodec();
            }

            session.addListener(new ServerListener(DEFAULT_NETWORK_CODEC));
        }
        this.client = false;
    }

    @Override
    public PacketRegistry getInboundPacketRegistry() {
        return this.inboundStateRegistry;
    }

    @Override
    public PacketRegistry getOutboundPacketRegistry() {
        return this.outboundStateRegistry;
    }

    /**
     * Resets the protocol states to {@link ProtocolState#HANDSHAKE}.
     */
    public void resetStates() {
        this.setInboundState(ProtocolState.HANDSHAKE);
        this.setOutboundState(ProtocolState.HANDSHAKE);
    }

    public void setInboundState(ProtocolState state) {
        log.debug("[{}] Setting inbound protocol state to: {}", client ? "Client" : "Server", state);

        this.inboundState = state;
        this.inboundStateRegistry = this.codec.getCodec(state);
    }

    public void setOutboundState(ProtocolState state) {
        log.debug("[{}] Setting outbound protocol state to: {}", client ? "Client" : "Server", state);

        this.outboundState = state;
        this.outboundStateRegistry = this.codec.getCodec(state);
    }

    public static CompoundTag loadNetworkCodec() {
        try (InputStream inputStream = Objects.requireNonNull(MinecraftProtocol.class.getClassLoader().getResourceAsStream("networkCodec.nbt")) ;
             DataInputStream stream = new DataInputStream(new GZIPInputStream(inputStream))) {
            return NBTIO.readTag(stream, TagLimiter.noop(), true, CompoundTag.class);
        } catch (Exception e) {
            throw new AssertionError("Unable to load network codec.", e);
        }
    }
}
