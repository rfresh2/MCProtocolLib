package com.github.steveice10.mc.protocol;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.*;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.*;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.*;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerDisplayScoreboardPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerScoreboardObjectivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerTeamPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerUpdateScorePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*;
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
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.crypt.AESEncryption;
import com.github.steveice10.packetlib.crypt.PacketEncryption;
import com.github.steveice10.packetlib.packet.DefaultPacketHeader;
import com.github.steveice10.packetlib.packet.PacketDefinition;
import com.github.steveice10.packetlib.packet.PacketHeader;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import lombok.Getter;
import lombok.Setter;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.UUID;

public class MinecraftProtocol extends PacketProtocol {
    private SubProtocol subProtocol = SubProtocol.HANDSHAKE;
    private PacketHeader header = new DefaultPacketHeader();
    private AESEncryption encrypt;

    private SubProtocol targetSubProtocol;
    private GameProfile profile;
    private String clientToken = "";
    private String accessToken = "";

    @Getter
    @Setter
    private boolean useDefaultListeners = true;

    public MinecraftProtocol() {
        this(SubProtocol.STATUS);
        this.setSubProtocol(SubProtocol.HANDSHAKE);
    }

    public MinecraftProtocol(SubProtocol subProtocol) {
        if(subProtocol != SubProtocol.LOGIN && subProtocol != SubProtocol.STATUS) {
            throw new IllegalArgumentException("Only login and status modes are permitted.");
        }

        this.targetSubProtocol = subProtocol;
        if(subProtocol == SubProtocol.LOGIN) {
            this.profile = new GameProfile((UUID) null, "Player");
        }
    }

    public MinecraftProtocol(String username) {
        this(SubProtocol.LOGIN);
        this.profile = new GameProfile((UUID) null, username);
    }

    public MinecraftProtocol(GameProfile profile, String clientToken, String accessToken) {
        this(SubProtocol.LOGIN);
        this.profile = profile;
        this.clientToken = clientToken;
        this.accessToken = accessToken;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public String getClientToken() {
        return this.clientToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    @Override
    public String getSRVRecordPrefix() {
        return "_minecraft";
    }

    @Override
    public PacketHeader getPacketHeader() {
        return this.header;
    }

    @Override
    public void newClientSession(Session session) {
        if(this.profile != null) {
            session.setFlag(MinecraftConstants.PROFILE_KEY, this.profile);
            session.setFlag(MinecraftConstants.ACCESS_TOKEN_KEY, this.accessToken);
        }

        this.setSubProtocol(this.subProtocol);
        if(this.useDefaultListeners) {
            session.addListener(new ClientListener(this.targetSubProtocol));
        }
    }

    @Override
    public void newServerSession(Server server, Session session) {
        this.setSubProtocol(SubProtocol.HANDSHAKE);
        if(this.useDefaultListeners) {
            session.addListener(new ServerListener());
        }
    }

    public SubProtocol getSubProtocol() {
        return this.subProtocol;
    }

    protected void setSubProtocol(SubProtocol subProtocol) {
        this.clearPackets();
        switch(subProtocol) {
            case HANDSHAKE:
                this.initHandshake();
                break;
            case LOGIN:
                this.initLogin();
                break;
            case GAME:
                this.initGame();
                break;
            case STATUS:
                this.initStatus();
                break;
        }

        this.subProtocol = subProtocol;
    }

    private void initHandshake() {
        this.registerServerbound(new PacketDefinition<>(0, HandshakePacket.class, HandshakePacket::new));
    }

    private void initLogin() {
        this.registerClientbound(new PacketDefinition<>(0x00, LoginDisconnectPacket.class, LoginDisconnectPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x01, EncryptionRequestPacket.class, EncryptionRequestPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x02, LoginSuccessPacket.class, LoginSuccessPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x03, LoginSetCompressionPacket.class, LoginSetCompressionPacket::new));

        this.registerServerbound(new PacketDefinition<>(0x00, LoginStartPacket.class, LoginStartPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x01, EncryptionResponsePacket.class, EncryptionResponsePacket::new));
    }

    private void initGame() {
        this.registerClientbound(new PacketDefinition<>(0x00, ServerSpawnObjectPacket.class, ServerSpawnObjectPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x01, ServerSpawnExpOrbPacket.class, ServerSpawnExpOrbPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x02, ServerSpawnGlobalEntityPacket.class, ServerSpawnGlobalEntityPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x03, ServerSpawnMobPacket.class, ServerSpawnMobPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x04, ServerSpawnPaintingPacket.class, ServerSpawnPaintingPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x05, ServerSpawnPlayerPacket.class, ServerSpawnPlayerPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x06, ServerEntityAnimationPacket.class, ServerEntityAnimationPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x07, ServerStatisticsPacket.class, ServerStatisticsPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x08, ServerBlockBreakAnimPacket.class, ServerBlockBreakAnimPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x09, ServerUpdateTileEntityPacket.class, ServerUpdateTileEntityPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x0A, ServerBlockValuePacket.class, ServerBlockValuePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x0B, ServerBlockChangePacket.class, ServerBlockChangePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x0C, ServerBossBarPacket.class, ServerBossBarPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x0D, ServerDifficultyPacket.class, ServerDifficultyPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x0E, ServerTabCompletePacket.class, ServerTabCompletePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x0F, ServerChatPacket.class, ServerChatPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x10, ServerMultiBlockChangePacket.class, ServerMultiBlockChangePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x11, ServerConfirmTransactionPacket.class, ServerConfirmTransactionPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x12, ServerCloseWindowPacket.class, ServerCloseWindowPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x13, ServerOpenWindowPacket.class, ServerOpenWindowPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x14, ServerWindowItemsPacket.class, ServerWindowItemsPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x15, ServerWindowPropertyPacket.class, ServerWindowPropertyPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x16, ServerSetSlotPacket.class, ServerSetSlotPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x17, ServerSetCooldownPacket.class, ServerSetCooldownPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x18, ServerPluginMessagePacket.class, ServerPluginMessagePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x19, ServerPlaySoundPacket.class, ServerPlaySoundPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x1A, ServerDisconnectPacket.class, ServerDisconnectPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x1B, ServerEntityStatusPacket.class, ServerEntityStatusPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x1C, ServerExplosionPacket.class, ServerExplosionPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x1D, ServerUnloadChunkPacket.class, ServerUnloadChunkPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x1E, ServerNotifyClientPacket.class, ServerNotifyClientPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x1F, ServerKeepAlivePacket.class, ServerKeepAlivePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x20, ServerChunkDataPacket.class, ServerChunkDataPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x21, ServerPlayEffectPacket.class, ServerPlayEffectPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x22, ServerSpawnParticlePacket.class, ServerSpawnParticlePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x23, ServerJoinGamePacket.class, ServerJoinGamePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x24, ServerMapDataPacket.class, ServerMapDataPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x25, ServerEntityMovementPacket.class, ServerEntityMovementPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x26, ServerEntityPositionPacket.class, ServerEntityPositionPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x27, ServerEntityPositionRotationPacket.class, ServerEntityPositionRotationPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x28, ServerEntityRotationPacket.class, ServerEntityRotationPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x29, ServerVehicleMovePacket.class, ServerVehicleMovePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x2A, ServerOpenTileEntityEditorPacket.class, ServerOpenTileEntityEditorPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x2B, ServerPreparedCraftingGridPacket.class, ServerPreparedCraftingGridPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x2C, ServerPlayerAbilitiesPacket.class, ServerPlayerAbilitiesPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x2D, ServerCombatPacket.class, ServerCombatPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x2E, ServerPlayerListEntryPacket.class, ServerPlayerListEntryPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x2F, ServerPlayerPositionRotationPacket.class, ServerPlayerPositionRotationPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x30, ServerPlayerUseBedPacket.class, ServerPlayerUseBedPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x31, ServerUnlockRecipesPacket.class, ServerUnlockRecipesPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x32, ServerEntityDestroyPacket.class, ServerEntityDestroyPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x33, ServerEntityRemoveEffectPacket.class, ServerEntityRemoveEffectPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x34, ServerResourcePackSendPacket.class, ServerResourcePackSendPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x35, ServerRespawnPacket.class, ServerRespawnPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x36, ServerEntityHeadLookPacket.class, ServerEntityHeadLookPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x37, ServerAdvancementTabPacket.class, ServerAdvancementTabPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x38, ServerWorldBorderPacket.class, ServerWorldBorderPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x39, ServerSwitchCameraPacket.class, ServerSwitchCameraPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x3A, ServerPlayerChangeHeldItemPacket.class, ServerPlayerChangeHeldItemPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x3B, ServerDisplayScoreboardPacket.class, ServerDisplayScoreboardPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x3C, ServerEntityMetadataPacket.class, ServerEntityMetadataPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x3D, ServerEntityAttachPacket.class, ServerEntityAttachPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x3E, ServerEntityVelocityPacket.class, ServerEntityVelocityPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x3F, ServerEntityEquipmentPacket.class, ServerEntityEquipmentPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x40, ServerPlayerSetExperiencePacket.class, ServerPlayerSetExperiencePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x41, ServerPlayerHealthPacket.class, ServerPlayerHealthPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x42, ServerScoreboardObjectivePacket.class, ServerScoreboardObjectivePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x43, ServerEntitySetPassengersPacket.class, ServerEntitySetPassengersPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x44, ServerTeamPacket.class, ServerTeamPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x45, ServerUpdateScorePacket.class, ServerUpdateScorePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x46, ServerSpawnPositionPacket.class, ServerSpawnPositionPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x47, ServerUpdateTimePacket.class, ServerUpdateTimePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x48, ServerTitlePacket.class, ServerTitlePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x49, ServerPlayBuiltinSoundPacket.class, ServerPlayBuiltinSoundPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x4A, ServerPlayerListDataPacket.class, ServerPlayerListDataPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x4B, ServerEntityCollectItemPacket.class, ServerEntityCollectItemPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x4C, ServerEntityTeleportPacket.class, ServerEntityTeleportPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x4D, ServerAdvancementsPacket.class, ServerAdvancementsPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x4E, ServerEntityPropertiesPacket.class, ServerEntityPropertiesPacket::new));
        this.registerClientbound(new PacketDefinition<>(0x4F, ServerEntityEffectPacket.class, ServerEntityEffectPacket::new));

        this.registerServerbound(new PacketDefinition<>(0x00, ClientTeleportConfirmPacket.class, ClientTeleportConfirmPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x01, ClientTabCompletePacket.class, ClientTabCompletePacket::new));
        this.registerServerbound(new PacketDefinition<>(0x02, ClientChatPacket.class, ClientChatPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x03, ClientRequestPacket.class, ClientRequestPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x04, ClientSettingsPacket.class, ClientSettingsPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x05, ClientConfirmTransactionPacket.class, ClientConfirmTransactionPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x06, ClientEnchantItemPacket.class, ClientEnchantItemPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x07, ClientWindowActionPacket.class, ClientWindowActionPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x08, ClientCloseWindowPacket.class, ClientCloseWindowPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x09, ClientPluginMessagePacket.class, ClientPluginMessagePacket::new));
        this.registerServerbound(new PacketDefinition<>(0x0A, ClientPlayerInteractEntityPacket.class, ClientPlayerInteractEntityPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x0B, ClientKeepAlivePacket.class, ClientKeepAlivePacket::new));
        this.registerServerbound(new PacketDefinition<>(0x0C, ClientPlayerMovementPacket.class, ClientPlayerMovementPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x0D, ClientPlayerPositionPacket.class, ClientPlayerPositionPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x0E, ClientPlayerPositionRotationPacket.class, ClientPlayerPositionRotationPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x0F, ClientPlayerRotationPacket.class, ClientPlayerRotationPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x10, ClientVehicleMovePacket.class, ClientVehicleMovePacket::new));
        this.registerServerbound(new PacketDefinition<>(0x11, ClientSteerBoatPacket.class, ClientSteerBoatPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x12, ClientPrepareCraftingGridPacket.class, ClientPrepareCraftingGridPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x13, ClientPlayerAbilitiesPacket.class, ClientPlayerAbilitiesPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x14, ClientPlayerActionPacket.class, ClientPlayerActionPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x15, ClientPlayerStatePacket.class, ClientPlayerStatePacket::new));
        this.registerServerbound(new PacketDefinition<>(0x16, ClientSteerVehiclePacket.class, ClientSteerVehiclePacket::new));
        this.registerServerbound(new PacketDefinition<>(0x17, ClientCraftingBookDataPacket.class, ClientCraftingBookDataPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x18, ClientResourcePackStatusPacket.class, ClientResourcePackStatusPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x19, ClientAdvancementTabPacket.class, ClientAdvancementTabPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x1A, ClientPlayerChangeHeldItemPacket.class, ClientPlayerChangeHeldItemPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x1B, ClientCreativeInventoryActionPacket.class, ClientCreativeInventoryActionPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x1C, ClientUpdateSignPacket.class, ClientUpdateSignPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x1D, ClientPlayerSwingArmPacket.class, ClientPlayerSwingArmPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x1E, ClientSpectatePacket.class, ClientSpectatePacket::new));
        this.registerServerbound(new PacketDefinition<>(0x1F, ClientPlayerPlaceBlockPacket.class, ClientPlayerPlaceBlockPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x20, ClientPlayerUseItemPacket.class, ClientPlayerUseItemPacket::new));
    }

    private void initStatus() {
        this.registerClientbound(new PacketDefinition<>(0x00, StatusResponsePacket.class, StatusResponsePacket::new));
        this.registerClientbound(new PacketDefinition<>(0x01, StatusPongPacket.class, StatusPongPacket::new));

        this.registerServerbound(new PacketDefinition<>(0x00, StatusQueryPacket.class, StatusQueryPacket::new));
        this.registerServerbound(new PacketDefinition<>(0x01, StatusPingPacket.class, StatusPingPacket::new));
    }
}
