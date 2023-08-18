package com.github.steveice10.mc.protocol.codec;

import com.github.steveice10.mc.protocol.data.ProtocolState;
import com.github.steveice10.mc.protocol.data.game.level.event.LevelEventType;
import com.github.steveice10.mc.protocol.data.game.level.sound.BuiltinSound;
import com.github.steveice10.mc.protocol.packet.handshake.serverbound.ClientIntentionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddExperienceOrbPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.inventory.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.border.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetDisplayObjectivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetObjectivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetPlayerTeamPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetScorePacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.inventory.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.level.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.*;
import com.github.steveice10.mc.protocol.packet.login.clientbound.*;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundCustomQueryPacket;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundHelloPacket;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundKeyPacket;
import com.github.steveice10.mc.protocol.packet.status.clientbound.ClientboundPongResponsePacket;
import com.github.steveice10.mc.protocol.packet.status.clientbound.ClientboundStatusResponsePacket;
import com.github.steveice10.mc.protocol.packet.status.serverbound.ServerboundPingRequestPacket;
import com.github.steveice10.mc.protocol.packet.status.serverbound.ServerboundStatusRequestPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public class MinecraftCodec {
    private static final Int2ObjectMap<LevelEventType> LEVEL_EVENTS = new Int2ObjectOpenHashMap<>();
    private static final Map<String, BuiltinSound> SOUND_NAMES = new HashMap<>();

    static {
        for (LevelEventType levelEvent : LevelEventType.values()) {
            LEVEL_EVENTS.put(levelEvent.getId(), levelEvent);
        }

        for (BuiltinSound sound : BuiltinSound.values()) {
            SOUND_NAMES.put(sound.getName(), sound);
        }
    }

    public static final PacketCodec CODEC = PacketCodec.builder()
            .protocolVersion(763)
            .helper(() -> new MinecraftCodecHelper(LEVEL_EVENTS, SOUND_NAMES))
            .minecraftVersion("1.20")
            .state(ProtocolState.HANDSHAKE, PacketStateCodec.builder()
                    .registerServerboundPacket(0x00, ClientIntentionPacket.class, ClientIntentionPacket::new)
            )
            .state(ProtocolState.LOGIN, PacketStateCodec.builder()
                    .registerClientboundPacket(0x00, ClientboundLoginDisconnectPacket.class, ClientboundLoginDisconnectPacket::new)
                    .registerClientboundPacket(0x01, ClientboundHelloPacket.class, ClientboundHelloPacket::new)
                    .registerClientboundPacket(0x02, ClientboundGameProfilePacket.class, ClientboundGameProfilePacket::new)
                    .registerClientboundPacket(0x03, ClientboundLoginCompressionPacket.class, ClientboundLoginCompressionPacket::new)
                    .registerClientboundPacket(0x04, ClientboundCustomQueryPacket.class, ClientboundCustomQueryPacket::new)
                    .registerServerboundPacket(0x00, ServerboundHelloPacket.class, ServerboundHelloPacket::new)
                    .registerServerboundPacket(0x01, ServerboundKeyPacket.class, ServerboundKeyPacket::new)
                    .registerServerboundPacket(0x02, ServerboundCustomQueryPacket.class, ServerboundCustomQueryPacket::new)
            ).state(ProtocolState.STATUS, PacketStateCodec.builder()
                    .registerClientboundPacket(0x00, ClientboundStatusResponsePacket.class, ClientboundStatusResponsePacket::new)
                    .registerClientboundPacket(0x01, ClientboundPongResponsePacket.class, ClientboundPongResponsePacket::new)
                    .registerServerboundPacket(0x00, ServerboundStatusRequestPacket.class, ServerboundStatusRequestPacket::new)
                    .registerServerboundPacket(0x01, ServerboundPingRequestPacket.class, ServerboundPingRequestPacket::new)
            ).state(ProtocolState.GAME, PacketStateCodec.builder()
                    .registerClientboundPacket(0x00, ClientboundDelimiterPacket.class, ClientboundDelimiterPacket::new)
                    .registerClientboundPacket(0x01, ClientboundAddEntityPacket.class, ClientboundAddEntityPacket::new)
                    .registerClientboundPacket(0x02, ClientboundAddExperienceOrbPacket.class, ClientboundAddExperienceOrbPacket::new)
                    .registerClientboundPacket(0x03, ClientboundAddPlayerPacket.class, ClientboundAddPlayerPacket::new)
                    .registerClientboundPacket(0x04, ClientboundAnimatePacket.class, ClientboundAnimatePacket::new)
                    .registerClientboundPacket(0x05, ClientboundAwardStatsPacket.class, ClientboundAwardStatsPacket::new)
                    .registerClientboundPacket(0x06, ClientboundBlockChangedAckPacket.class, ClientboundBlockChangedAckPacket::new)
                    .registerClientboundPacket(0x07, ClientboundBlockDestructionPacket.class, ClientboundBlockDestructionPacket::new)
                    .registerClientboundPacket(0x08, ClientboundBlockEntityDataPacket.class, ClientboundBlockEntityDataPacket::new)
                    .registerClientboundPacket(0x09, ClientboundBlockEventPacket.class, ClientboundBlockEventPacket::new)
                    .registerClientboundPacket(0x0A, ClientboundBlockUpdatePacket.class, ClientboundBlockUpdatePacket::new)
                    .registerClientboundPacket(0x0B, ClientboundBossEventPacket.class, ClientboundBossEventPacket::new)
                    .registerClientboundPacket(0x0C, ClientboundChangeDifficultyPacket.class, ClientboundChangeDifficultyPacket::new)
                    .registerClientboundPacket(0x0D, ClientboundChunksBiomesPacket.class, ClientboundChunksBiomesPacket::new)
                    .registerClientboundPacket(0x0E, ClientboundClearTitlesPacket.class, ClientboundClearTitlesPacket::new)
                    .registerClientboundPacket(0x0F, ClientboundCommandSuggestionsPacket.class, ClientboundCommandSuggestionsPacket::new)
                    .registerClientboundPacket(0x10, ClientboundCommandsPacket.class, ClientboundCommandsPacket::new)
                    .registerClientboundPacket(0x11, ClientboundContainerClosePacket.class, ClientboundContainerClosePacket::new)
                    .registerClientboundPacket(0x12, ClientboundContainerSetContentPacket.class, ClientboundContainerSetContentPacket::new)
                    .registerClientboundPacket(0x13, ClientboundContainerSetDataPacket.class, ClientboundContainerSetDataPacket::new)
                    .registerClientboundPacket(0x14, ClientboundContainerSetSlotPacket.class, ClientboundContainerSetSlotPacket::new)
                    .registerClientboundPacket(0x15, ClientboundCooldownPacket.class, ClientboundCooldownPacket::new)
                    .registerClientboundPacket(0x16, ClientboundCustomChatCompletionsPacket.class, ClientboundCustomChatCompletionsPacket::new)
                    .registerClientboundPacket(0x17, ClientboundCustomPayloadPacket.class, ClientboundCustomPayloadPacket::new)
                    .registerClientboundPacket(0x18, ClientboundDamageEventPacket.class, ClientboundDamageEventPacket::new)
                    .registerClientboundPacket(0x19, ClientboundDeleteChatPacket.class, ClientboundDeleteChatPacket::new)
                    .registerClientboundPacket(0x1A, ClientboundDisconnectPacket.class, ClientboundDisconnectPacket::new)
                    .registerClientboundPacket(0x1B, ClientboundDisguisedChatPacket.class, ClientboundDisguisedChatPacket::new)
                    .registerClientboundPacket(0x1C, ClientboundEntityEventPacket.class, ClientboundEntityEventPacket::new)
                    .registerClientboundPacket(0x1D, ClientboundExplodePacket.class, ClientboundExplodePacket::new)
                    .registerClientboundPacket(0x1E, ClientboundForgetLevelChunkPacket.class, ClientboundForgetLevelChunkPacket::new)
                    .registerClientboundPacket(0x1F, ClientboundGameEventPacket.class, ClientboundGameEventPacket::new)
                    .registerClientboundPacket(0x20, ClientboundHorseScreenOpenPacket.class, ClientboundHorseScreenOpenPacket::new)
                    .registerClientboundPacket(0x21, ClientboundHurtAnimationPacket.class, ClientboundHurtAnimationPacket::new)
                    .registerClientboundPacket(0x22, ClientboundInitializeBorderPacket.class, ClientboundInitializeBorderPacket::new)
                    .registerClientboundPacket(0x23, ClientboundKeepAlivePacket.class, ClientboundKeepAlivePacket::new)
                    .registerClientboundPacket(0x24, ClientboundLevelChunkWithLightPacket.class, ClientboundLevelChunkWithLightPacket::new)
                    .registerClientboundPacket(0x25, ClientboundLevelEventPacket.class, ClientboundLevelEventPacket::new)
                    .registerClientboundPacket(0x26, ClientboundLevelParticlesPacket.class, ClientboundLevelParticlesPacket::new)
                    .registerClientboundPacket(0x27, ClientboundLightUpdatePacket.class, ClientboundLightUpdatePacket::new)
                    .registerClientboundPacket(0x28, ClientboundLoginPacket.class, ClientboundLoginPacket::new)
                    .registerClientboundPacket(0x29, ClientboundMapItemDataPacket.class, ClientboundMapItemDataPacket::new)
                    .registerClientboundPacket(0x2A, ClientboundMerchantOffersPacket.class, ClientboundMerchantOffersPacket::new)
                    .registerClientboundPacket(0x2B, ClientboundMoveEntityPosPacket.class, ClientboundMoveEntityPosPacket::new)
                    .registerClientboundPacket(0x2C, ClientboundMoveEntityPosRotPacket.class, ClientboundMoveEntityPosRotPacket::new)
                    .registerClientboundPacket(0x2D, ClientboundMoveEntityRotPacket.class, ClientboundMoveEntityRotPacket::new)
                    .registerClientboundPacket(0x2E, ClientboundMoveVehiclePacket.class, ClientboundMoveVehiclePacket::new)
                    .registerClientboundPacket(0x2F, ClientboundOpenBookPacket.class, ClientboundOpenBookPacket::new)
                    .registerClientboundPacket(0x30, ClientboundOpenScreenPacket.class, ClientboundOpenScreenPacket::new)
                    .registerClientboundPacket(0x31, ClientboundOpenSignEditorPacket.class, ClientboundOpenSignEditorPacket::new)
                    .registerClientboundPacket(0x32, ClientboundPingPacket.class, ClientboundPingPacket::new)
                    .registerClientboundPacket(0x33, ClientboundPlaceGhostRecipePacket.class, ClientboundPlaceGhostRecipePacket::new)
                    .registerClientboundPacket(0x34, ClientboundPlayerAbilitiesPacket.class, ClientboundPlayerAbilitiesPacket::new)
                    .registerClientboundPacket(0x35, ClientboundPlayerChatPacket.class, ClientboundPlayerChatPacket::new)
                    .registerClientboundPacket(0x36, ClientboundPlayerCombatEndPacket.class, ClientboundPlayerCombatEndPacket::new)
                    .registerClientboundPacket(0x37, ClientboundPlayerCombatEnterPacket.class, ClientboundPlayerCombatEnterPacket::new)
                    .registerClientboundPacket(0x38, ClientboundPlayerCombatKillPacket.class, ClientboundPlayerCombatKillPacket::new)
                    .registerClientboundPacket(0x39, ClientboundPlayerInfoRemovePacket.class, ClientboundPlayerInfoRemovePacket::new)
                    .registerClientboundPacket(0x3A, ClientboundPlayerInfoUpdatePacket.class, ClientboundPlayerInfoUpdatePacket::new)
                    .registerClientboundPacket(0x3B, ClientboundPlayerLookAtPacket.class, ClientboundPlayerLookAtPacket::new)
                    .registerClientboundPacket(0x3C, ClientboundPlayerPositionPacket.class, ClientboundPlayerPositionPacket::new)
                    .registerClientboundPacket(0x3D, ClientboundRecipePacket.class, ClientboundRecipePacket::new)
                    .registerClientboundPacket(0x3E, ClientboundRemoveEntitiesPacket.class, ClientboundRemoveEntitiesPacket::new)
                    .registerClientboundPacket(0x3F, ClientboundRemoveMobEffectPacket.class, ClientboundRemoveMobEffectPacket::new)
                    .registerClientboundPacket(0x40, ClientboundResourcePackPacket.class, ClientboundResourcePackPacket::new)
                    .registerClientboundPacket(0x41, ClientboundRespawnPacket.class, ClientboundRespawnPacket::new)
                    .registerClientboundPacket(0x42, ClientboundRotateHeadPacket.class, ClientboundRotateHeadPacket::new)
                    .registerClientboundPacket(0x43, ClientboundSectionBlocksUpdatePacket.class, ClientboundSectionBlocksUpdatePacket::new)
                    .registerClientboundPacket(0x44, ClientboundSelectAdvancementsTabPacket.class, ClientboundSelectAdvancementsTabPacket::new)
                    .registerClientboundPacket(0x45, ClientboundServerDataPacket.class, ClientboundServerDataPacket::new)
                    .registerClientboundPacket(0x46, ClientboundSetActionBarTextPacket.class, ClientboundSetActionBarTextPacket::new)
                    .registerClientboundPacket(0x47, ClientboundSetBorderCenterPacket.class, ClientboundSetBorderCenterPacket::new)
                    .registerClientboundPacket(0x48, ClientboundSetBorderLerpSizePacket.class, ClientboundSetBorderLerpSizePacket::new)
                    .registerClientboundPacket(0x49, ClientboundSetBorderSizePacket.class, ClientboundSetBorderSizePacket::new)
                    .registerClientboundPacket(0x4A, ClientboundSetBorderWarningDelayPacket.class, ClientboundSetBorderWarningDelayPacket::new)
                    .registerClientboundPacket(0x4B, ClientboundSetBorderWarningDistancePacket.class, ClientboundSetBorderWarningDistancePacket::new)
                    .registerClientboundPacket(0x4C, ClientboundSetCameraPacket.class, ClientboundSetCameraPacket::new)
                    .registerClientboundPacket(0x4D, ClientboundSetCarriedItemPacket.class, ClientboundSetCarriedItemPacket::new)
                    .registerClientboundPacket(0x4E, ClientboundSetChunkCacheCenterPacket.class, ClientboundSetChunkCacheCenterPacket::new)
                    .registerClientboundPacket(0x4F, ClientboundSetChunkCacheRadiusPacket.class, ClientboundSetChunkCacheRadiusPacket::new)
                    .registerClientboundPacket(0x50, ClientboundSetDefaultSpawnPositionPacket.class, ClientboundSetDefaultSpawnPositionPacket::new)
                    .registerClientboundPacket(0x51, ClientboundSetDisplayObjectivePacket.class, ClientboundSetDisplayObjectivePacket::new)
                    .registerClientboundPacket(0x52, ClientboundSetEntityDataPacket.class, ClientboundSetEntityDataPacket::new)
                    .registerClientboundPacket(0x53, ClientboundSetEntityLinkPacket.class, ClientboundSetEntityLinkPacket::new)
                    .registerClientboundPacket(0x54, ClientboundSetEntityMotionPacket.class, ClientboundSetEntityMotionPacket::new)
                    .registerClientboundPacket(0x55, ClientboundSetEquipmentPacket.class, ClientboundSetEquipmentPacket::new)
                    .registerClientboundPacket(0x56, ClientboundSetExperiencePacket.class, ClientboundSetExperiencePacket::new)
                    .registerClientboundPacket(0x57, ClientboundSetHealthPacket.class, ClientboundSetHealthPacket::new)
                    .registerClientboundPacket(0x58, ClientboundSetObjectivePacket.class, ClientboundSetObjectivePacket::new)
                    .registerClientboundPacket(0x59, ClientboundSetPassengersPacket.class, ClientboundSetPassengersPacket::new)
                    .registerClientboundPacket(0x5A, ClientboundSetPlayerTeamPacket.class, ClientboundSetPlayerTeamPacket::new)
                    .registerClientboundPacket(0x5B, ClientboundSetScorePacket.class, ClientboundSetScorePacket::new)
                    .registerClientboundPacket(0x5C, ClientboundSetSimulationDistancePacket.class, ClientboundSetSimulationDistancePacket::new)
                    .registerClientboundPacket(0x5D, ClientboundSetSubtitleTextPacket.class, ClientboundSetSubtitleTextPacket::new)
                    .registerClientboundPacket(0x5E, ClientboundSetTimePacket.class, ClientboundSetTimePacket::new)
                    .registerClientboundPacket(0x5F, ClientboundSetTitleTextPacket.class, ClientboundSetTitleTextPacket::new)
                    .registerClientboundPacket(0x60, ClientboundSetTitlesAnimationPacket.class, ClientboundSetTitlesAnimationPacket::new)
                    .registerClientboundPacket(0x61, ClientboundSoundEntityPacket.class, ClientboundSoundEntityPacket::new)
                    .registerClientboundPacket(0x62, ClientboundSoundPacket.class, ClientboundSoundPacket::new)
                    .registerClientboundPacket(0x63, ClientboundStopSoundPacket.class, ClientboundStopSoundPacket::new)
                    .registerClientboundPacket(0x64, ClientboundSystemChatPacket.class, ClientboundSystemChatPacket::new)
                    .registerClientboundPacket(0x65, ClientboundTabListPacket.class, ClientboundTabListPacket::new)
                    .registerClientboundPacket(0x66, ClientboundTagQueryPacket.class, ClientboundTagQueryPacket::new)
                    .registerClientboundPacket(0x67, ClientboundTakeItemEntityPacket.class, ClientboundTakeItemEntityPacket::new)
                    .registerClientboundPacket(0x68, ClientboundTeleportEntityPacket.class, ClientboundTeleportEntityPacket::new)
                    .registerClientboundPacket(0x69, ClientboundUpdateAdvancementsPacket.class, ClientboundUpdateAdvancementsPacket::new)
                    .registerClientboundPacket(0x6A, ClientboundUpdateAttributesPacket.class, ClientboundUpdateAttributesPacket::new)
                    .registerClientboundPacket(0x6B, ClientboundUpdateEnabledFeaturesPacket.class, ClientboundUpdateEnabledFeaturesPacket::new)
                    .registerClientboundPacket(0x6C, ClientboundUpdateMobEffectPacket.class, ClientboundUpdateMobEffectPacket::new)
                    .registerClientboundPacket(0x6D, ClientboundUpdateRecipesPacket.class, ClientboundUpdateRecipesPacket::new)
                    .registerClientboundPacket(0x6E, ClientboundUpdateTagsPacket.class, ClientboundUpdateTagsPacket::new)
                    .registerServerboundPacket(0x00, ServerboundAcceptTeleportationPacket.class, ServerboundAcceptTeleportationPacket::new)
                    .registerServerboundPacket(0x01, ServerboundBlockEntityTagQuery.class, ServerboundBlockEntityTagQuery::new)
                    .registerServerboundPacket(0x02, ServerboundChangeDifficultyPacket.class, ServerboundChangeDifficultyPacket::new)
                    .registerServerboundPacket(0x03, ServerboundChatAckPacket.class, ServerboundChatAckPacket::new)
                    .registerServerboundPacket(0x04, ServerboundChatCommandPacket.class, ServerboundChatCommandPacket::new)
                    .registerServerboundPacket(0x05, ServerboundChatPacket.class, ServerboundChatPacket::new)
                    .registerServerboundPacket(0x06, ServerboundChatSessionUpdatePacket.class, ServerboundChatSessionUpdatePacket::new)
                    .registerServerboundPacket(0x07, ServerboundClientCommandPacket.class, ServerboundClientCommandPacket::new)
                    .registerServerboundPacket(0x08, ServerboundClientInformationPacket.class, ServerboundClientInformationPacket::new)
                    .registerServerboundPacket(0x09, ServerboundCommandSuggestionPacket.class, ServerboundCommandSuggestionPacket::new)
                    .registerServerboundPacket(0x0A, ServerboundContainerButtonClickPacket.class, ServerboundContainerButtonClickPacket::new)
                    .registerServerboundPacket(0x0B, ServerboundContainerClickPacket.class, ServerboundContainerClickPacket::new)
                    .registerServerboundPacket(0x0C, ServerboundContainerClosePacket.class, ServerboundContainerClosePacket::new)
                    .registerServerboundPacket(0x0D, ServerboundCustomPayloadPacket.class, ServerboundCustomPayloadPacket::new)
                    .registerServerboundPacket(0x0E, ServerboundEditBookPacket.class, ServerboundEditBookPacket::new)
                    .registerServerboundPacket(0x0F, ServerboundEntityTagQuery.class, ServerboundEntityTagQuery::new)
                    .registerServerboundPacket(0x10, ServerboundInteractPacket.class, ServerboundInteractPacket::new)
                    .registerServerboundPacket(0x11, ServerboundJigsawGeneratePacket.class, ServerboundJigsawGeneratePacket::new)
                    .registerServerboundPacket(0x12, ServerboundKeepAlivePacket.class, ServerboundKeepAlivePacket::new)
                    .registerServerboundPacket(0x13, ServerboundLockDifficultyPacket.class, ServerboundLockDifficultyPacket::new)
                    .registerServerboundPacket(0x14, ServerboundMovePlayerPosPacket.class, ServerboundMovePlayerPosPacket::new)
                    .registerServerboundPacket(0x15, ServerboundMovePlayerPosRotPacket.class, ServerboundMovePlayerPosRotPacket::new)
                    .registerServerboundPacket(0x16, ServerboundMovePlayerRotPacket.class, ServerboundMovePlayerRotPacket::new)
                    .registerServerboundPacket(0x17, ServerboundMovePlayerStatusOnlyPacket.class, ServerboundMovePlayerStatusOnlyPacket::new)
                    .registerServerboundPacket(0x18, ServerboundMoveVehiclePacket.class, ServerboundMoveVehiclePacket::new)
                    .registerServerboundPacket(0x19, ServerboundPaddleBoatPacket.class, ServerboundPaddleBoatPacket::new)
                    .registerServerboundPacket(0x1A, ServerboundPickItemPacket.class, ServerboundPickItemPacket::new)
                    .registerServerboundPacket(0x1B, ServerboundPlaceRecipePacket.class, ServerboundPlaceRecipePacket::new)
                    .registerServerboundPacket(0x1C, ServerboundPlayerAbilitiesPacket.class, ServerboundPlayerAbilitiesPacket::new)
                    .registerServerboundPacket(0x1D, ServerboundPlayerActionPacket.class, ServerboundPlayerActionPacket::new)
                    .registerServerboundPacket(0x1E, ServerboundPlayerCommandPacket.class, ServerboundPlayerCommandPacket::new)
                    .registerServerboundPacket(0x1F, ServerboundPlayerInputPacket.class, ServerboundPlayerInputPacket::new)
                    .registerServerboundPacket(0x20, ServerboundPongPacket.class, ServerboundPongPacket::new)
                    .registerServerboundPacket(0x21, ServerboundRecipeBookChangeSettingsPacket.class, ServerboundRecipeBookChangeSettingsPacket::new)
                    .registerServerboundPacket(0x22, ServerboundRecipeBookSeenRecipePacket.class, ServerboundRecipeBookSeenRecipePacket::new)
                    .registerServerboundPacket(0x23, ServerboundRenameItemPacket.class, ServerboundRenameItemPacket::new)
                    .registerServerboundPacket(0x24, ServerboundResourcePackPacket.class, ServerboundResourcePackPacket::new)
                    .registerServerboundPacket(0x25, ServerboundSeenAdvancementsPacket.class, ServerboundSeenAdvancementsPacket::new)
                    .registerServerboundPacket(0x26, ServerboundSelectTradePacket.class, ServerboundSelectTradePacket::new)
                    .registerServerboundPacket(0x27, ServerboundSetBeaconPacket.class, ServerboundSetBeaconPacket::new)
                    .registerServerboundPacket(0x28, ServerboundSetCarriedItemPacket.class, ServerboundSetCarriedItemPacket::new)
                    .registerServerboundPacket(0x29, ServerboundSetCommandBlockPacket.class, ServerboundSetCommandBlockPacket::new)
                    .registerServerboundPacket(0x2A, ServerboundSetCommandMinecartPacket.class, ServerboundSetCommandMinecartPacket::new)
                    .registerServerboundPacket(0x2B, ServerboundSetCreativeModeSlotPacket.class, ServerboundSetCreativeModeSlotPacket::new)
                    .registerServerboundPacket(0x2C, ServerboundSetJigsawBlockPacket.class, ServerboundSetJigsawBlockPacket::new)
                    .registerServerboundPacket(0x2D, ServerboundSetStructureBlockPacket.class, ServerboundSetStructureBlockPacket::new)
                    .registerServerboundPacket(0x2E, ServerboundSignUpdatePacket.class, ServerboundSignUpdatePacket::new)
                    .registerServerboundPacket(0x2F, ServerboundSwingPacket.class, ServerboundSwingPacket::new)
                    .registerServerboundPacket(0x30, ServerboundTeleportToEntityPacket.class, ServerboundTeleportToEntityPacket::new)
                    .registerServerboundPacket(0x31, ServerboundUseItemOnPacket.class, ServerboundUseItemOnPacket::new)
                    .registerServerboundPacket(0x32, ServerboundUseItemPacket.class, ServerboundUseItemPacket::new)
            )
            .build();
}
