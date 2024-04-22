package com.github.steveice10.mc.protocol.codec;

import com.github.steveice10.mc.protocol.data.ProtocolState;
import com.github.steveice10.mc.protocol.data.game.level.event.LevelEventType;
import com.github.steveice10.mc.protocol.data.game.level.sound.BuiltinSound;
import com.github.steveice10.mc.protocol.packet.common.clientbound.*;
import com.github.steveice10.mc.protocol.packet.common.serverbound.*;
import com.github.steveice10.mc.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;
import com.github.steveice10.mc.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;
import com.github.steveice10.mc.protocol.packet.configuration.clientbound.ClientboundResetChatPacket;
import com.github.steveice10.mc.protocol.packet.configuration.clientbound.ClientboundSelectKnownPacks;
import com.github.steveice10.mc.protocol.packet.configuration.serverbound.ServerboundFinishConfigurationPacket;
import com.github.steveice10.mc.protocol.packet.configuration.serverbound.ServerboundSelectKnownPacks;
import com.github.steveice10.mc.protocol.packet.handshake.serverbound.ClientIntentionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddExperienceOrbPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.inventory.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.border.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.inventory.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.level.*;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.*;
import com.github.steveice10.mc.protocol.packet.login.clientbound.*;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundCustomQueryAnswerPacket;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundHelloPacket;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundKeyPacket;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundLoginAcknowledgedPacket;
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
            .protocolVersion((1 << 30) | 190)
            .helper(() -> new MinecraftCodecHelper(LEVEL_EVENTS, SOUND_NAMES))
            .minecraftVersion("1.20.5-rc2")
            .state(ProtocolState.HANDSHAKE, PacketStateCodec.builder()
                    .registerServerboundPacket(ClientIntentionPacket.class, ClientIntentionPacket::new)
            )
            .state(ProtocolState.LOGIN, PacketStateCodec.builder()
                    .registerClientboundPacket(ClientboundLoginDisconnectPacket.class, ClientboundLoginDisconnectPacket::new)
                    .registerClientboundPacket(ClientboundHelloPacket.class, ClientboundHelloPacket::new)
                    .registerClientboundPacket(ClientboundGameProfilePacket.class, ClientboundGameProfilePacket::new)
                    .registerClientboundPacket(ClientboundLoginCompressionPacket.class, ClientboundLoginCompressionPacket::new)
                    .registerClientboundPacket(ClientboundCustomQueryPacket.class, ClientboundCustomQueryPacket::new)
                    .registerClientboundPacket(ClientboundCookieRequestPacket.class, ClientboundCookieRequestPacket::new)
                    .registerServerboundPacket(ServerboundHelloPacket.class, ServerboundHelloPacket::new)
                    .registerServerboundPacket(ServerboundKeyPacket.class, ServerboundKeyPacket::new)
                    .registerServerboundPacket(ServerboundCustomQueryAnswerPacket.class, ServerboundCustomQueryAnswerPacket::new)
                    .registerServerboundPacket(ServerboundLoginAcknowledgedPacket.class, ServerboundLoginAcknowledgedPacket::new)
                    .registerServerboundPacket(ServerboundCookieResponsePacket.class, ServerboundCookieResponsePacket::new)
            ).state(ProtocolState.STATUS, PacketStateCodec.builder()
                    .registerClientboundPacket(ClientboundStatusResponsePacket.class, ClientboundStatusResponsePacket::new)
                    .registerClientboundPacket(ClientboundPongResponsePacket.class, ClientboundPongResponsePacket::new)
                    .registerServerboundPacket(ServerboundStatusRequestPacket.class, ServerboundStatusRequestPacket::new)
                    .registerServerboundPacket(ServerboundPingRequestPacket.class, ServerboundPingRequestPacket::new)
            ).state(ProtocolState.CONFIGURATION, PacketStateCodec.builder()
                    .registerClientboundPacket(ClientboundCookieRequestPacket.class, ClientboundCookieRequestPacket::new)
                    .registerClientboundPacket(ClientboundCustomPayloadPacket.class, ClientboundCustomPayloadPacket::new)
                    .registerClientboundPacket(ClientboundDisconnectPacket.class, ClientboundDisconnectPacket::new)
                    .registerClientboundPacket(ClientboundFinishConfigurationPacket.class, ClientboundFinishConfigurationPacket::new)
                    .registerClientboundPacket(ClientboundKeepAlivePacket.class, ClientboundKeepAlivePacket::new)
                    .registerClientboundPacket(ClientboundPingPacket.class, ClientboundPingPacket::new)
                    .registerClientboundPacket(ClientboundResetChatPacket.class, ClientboundResetChatPacket::new)
                    .registerClientboundPacket(ClientboundRegistryDataPacket.class, ClientboundRegistryDataPacket::new)
                    .registerClientboundPacket(ClientboundResourcePackPopPacket.class, ClientboundResourcePackPopPacket::new)
                    .registerClientboundPacket(ClientboundResourcePackPushPacket.class, ClientboundResourcePackPushPacket::new)
                    .registerClientboundPacket(ClientboundStoreCookiePacket.class, ClientboundStoreCookiePacket::new)
                    .registerClientboundPacket(ClientboundTransferPacket.class, ClientboundTransferPacket::new)
                    .registerClientboundPacket(ClientboundUpdateEnabledFeaturesPacket.class, ClientboundUpdateEnabledFeaturesPacket::new)
                    .registerClientboundPacket(ClientboundUpdateTagsPacket.class, ClientboundUpdateTagsPacket::new)
                    .registerClientboundPacket(ClientboundSelectKnownPacks.class, ClientboundSelectKnownPacks::new)
                    .registerServerboundPacket(ServerboundClientInformationPacket.class, ServerboundClientInformationPacket::new)
                    .registerServerboundPacket(ServerboundCookieResponsePacket.class, ServerboundCookieResponsePacket::new)
                    .registerServerboundPacket(ServerboundCustomPayloadPacket.class, ServerboundCustomPayloadPacket::new)
                    .registerServerboundPacket(ServerboundFinishConfigurationPacket.class, ServerboundFinishConfigurationPacket::new)
                    .registerServerboundPacket(ServerboundKeepAlivePacket.class, ServerboundKeepAlivePacket::new)
                    .registerServerboundPacket(ServerboundPongPacket.class, ServerboundPongPacket::new)
                    .registerServerboundPacket(ServerboundResourcePackPacket.class, ServerboundResourcePackPacket::new)
                    .registerServerboundPacket(ServerboundSelectKnownPacks.class, ServerboundSelectKnownPacks::new)
            ).state(ProtocolState.GAME, PacketStateCodec.builder()
                    .registerClientboundPacket(ClientboundDelimiterPacket.class, ClientboundDelimiterPacket::new)
                    .registerClientboundPacket(ClientboundAddEntityPacket.class, ClientboundAddEntityPacket::new)
                    .registerClientboundPacket(ClientboundAddExperienceOrbPacket.class, ClientboundAddExperienceOrbPacket::new)
                    .registerClientboundPacket(ClientboundAnimatePacket.class, ClientboundAnimatePacket::new)
                    .registerClientboundPacket(ClientboundAwardStatsPacket.class, ClientboundAwardStatsPacket::new)
                    .registerClientboundPacket(ClientboundBlockChangedAckPacket.class, ClientboundBlockChangedAckPacket::new)
                    .registerClientboundPacket(ClientboundBlockDestructionPacket.class, ClientboundBlockDestructionPacket::new)
                    .registerClientboundPacket(ClientboundBlockEntityDataPacket.class, ClientboundBlockEntityDataPacket::new)
                    .registerClientboundPacket(ClientboundBlockEventPacket.class, ClientboundBlockEventPacket::new)
                    .registerClientboundPacket(ClientboundBlockUpdatePacket.class, ClientboundBlockUpdatePacket::new)
                    .registerClientboundPacket(ClientboundBossEventPacket.class, ClientboundBossEventPacket::new)
                    .registerClientboundPacket(ClientboundChangeDifficultyPacket.class, ClientboundChangeDifficultyPacket::new)
                    .registerClientboundPacket(ClientboundChunkBatchFinishedPacket.class, ClientboundChunkBatchFinishedPacket::new)
                    .registerClientboundPacket(ClientboundChunkBatchStartPacket.class, ClientboundChunkBatchStartPacket::new)
                    .registerClientboundPacket(ClientboundChunksBiomesPacket.class, ClientboundChunksBiomesPacket::new)
                    .registerClientboundPacket(ClientboundClearTitlesPacket.class, ClientboundClearTitlesPacket::new)
                    .registerClientboundPacket(ClientboundCommandSuggestionsPacket.class, ClientboundCommandSuggestionsPacket::new)
                    .registerClientboundPacket(ClientboundCommandsPacket.class, ClientboundCommandsPacket::new)
                    .registerClientboundPacket(ClientboundContainerClosePacket.class, ClientboundContainerClosePacket::new)
                    .registerClientboundPacket(ClientboundContainerSetContentPacket.class, ClientboundContainerSetContentPacket::new)
                    .registerClientboundPacket(ClientboundContainerSetDataPacket.class, ClientboundContainerSetDataPacket::new)
                    .registerClientboundPacket(ClientboundContainerSetSlotPacket.class, ClientboundContainerSetSlotPacket::new)
                    .registerClientboundPacket(ClientboundCookieRequestPacket.class, ClientboundCookieRequestPacket::new)
                    .registerClientboundPacket(ClientboundCooldownPacket.class, ClientboundCooldownPacket::new)
                    .registerClientboundPacket(ClientboundCustomChatCompletionsPacket.class, ClientboundCustomChatCompletionsPacket::new)
                    .registerClientboundPacket(ClientboundCustomPayloadPacket.class, ClientboundCustomPayloadPacket::new)
                    .registerClientboundPacket(ClientboundDamageEventPacket.class, ClientboundDamageEventPacket::new)
                    .registerClientboundPacket(ClientboundDebugSamplePacket.class, ClientboundDebugSamplePacket::new)
                    .registerClientboundPacket(ClientboundDeleteChatPacket.class, ClientboundDeleteChatPacket::new)
                    .registerClientboundPacket(ClientboundDisconnectPacket.class, ClientboundDisconnectPacket::new)
                    .registerClientboundPacket(ClientboundDisguisedChatPacket.class, ClientboundDisguisedChatPacket::new)
                    .registerClientboundPacket(ClientboundEntityEventPacket.class, ClientboundEntityEventPacket::new)
                    .registerClientboundPacket(ClientboundExplodePacket.class, ClientboundExplodePacket::new)
                    .registerClientboundPacket(ClientboundForgetLevelChunkPacket.class, ClientboundForgetLevelChunkPacket::new)
                    .registerClientboundPacket(ClientboundGameEventPacket.class, ClientboundGameEventPacket::new)
                    .registerClientboundPacket(ClientboundHorseScreenOpenPacket.class, ClientboundHorseScreenOpenPacket::new)
                    .registerClientboundPacket(ClientboundHurtAnimationPacket.class, ClientboundHurtAnimationPacket::new)
                    .registerClientboundPacket(ClientboundInitializeBorderPacket.class, ClientboundInitializeBorderPacket::new)
                    .registerClientboundPacket(ClientboundKeepAlivePacket.class, ClientboundKeepAlivePacket::new)
                    .registerClientboundPacket(ClientboundLevelChunkWithLightPacket.class, ClientboundLevelChunkWithLightPacket::new)
                    .registerClientboundPacket(ClientboundLevelEventPacket.class, ClientboundLevelEventPacket::new)
                    .registerClientboundPacket(ClientboundLevelParticlesPacket.class, ClientboundLevelParticlesPacket::new)
                    .registerClientboundPacket(ClientboundLightUpdatePacket.class, ClientboundLightUpdatePacket::new)
                    .registerClientboundPacket(ClientboundLoginPacket.class, ClientboundLoginPacket::new)
                    .registerClientboundPacket(ClientboundMapItemDataPacket.class, ClientboundMapItemDataPacket::new)
                    .registerClientboundPacket(ClientboundMerchantOffersPacket.class, ClientboundMerchantOffersPacket::new)
                    .registerClientboundPacket(ClientboundMoveEntityPosPacket.class, ClientboundMoveEntityPosPacket::new)
                    .registerClientboundPacket(ClientboundMoveEntityPosRotPacket.class, ClientboundMoveEntityPosRotPacket::new)
                    .registerClientboundPacket(ClientboundMoveEntityRotPacket.class, ClientboundMoveEntityRotPacket::new)
                    .registerClientboundPacket(ClientboundMoveVehiclePacket.class, ClientboundMoveVehiclePacket::new)
                    .registerClientboundPacket(ClientboundOpenBookPacket.class, ClientboundOpenBookPacket::new)
                    .registerClientboundPacket(ClientboundOpenScreenPacket.class, ClientboundOpenScreenPacket::new)
                    .registerClientboundPacket(ClientboundOpenSignEditorPacket.class, ClientboundOpenSignEditorPacket::new)
                    .registerClientboundPacket(ClientboundPingPacket.class, ClientboundPingPacket::new)
                    .registerClientboundPacket(ClientboundPongResponsePacket.class, ClientboundPongResponsePacket::new)
                    .registerClientboundPacket(ClientboundPlaceGhostRecipePacket.class, ClientboundPlaceGhostRecipePacket::new)
                    .registerClientboundPacket(ClientboundPlayerAbilitiesPacket.class, ClientboundPlayerAbilitiesPacket::new)
                    .registerClientboundPacket(ClientboundPlayerChatPacket.class, ClientboundPlayerChatPacket::new)
                    .registerClientboundPacket(ClientboundPlayerCombatEndPacket.class, ClientboundPlayerCombatEndPacket::new)
                    .registerClientboundPacket(ClientboundPlayerCombatEnterPacket.class, ClientboundPlayerCombatEnterPacket::new)
                    .registerClientboundPacket(ClientboundPlayerCombatKillPacket.class, ClientboundPlayerCombatKillPacket::new)
                    .registerClientboundPacket(ClientboundPlayerInfoRemovePacket.class, ClientboundPlayerInfoRemovePacket::new)
                    .registerClientboundPacket(ClientboundPlayerInfoUpdatePacket.class, ClientboundPlayerInfoUpdatePacket::new)
                    .registerClientboundPacket(ClientboundPlayerLookAtPacket.class, ClientboundPlayerLookAtPacket::new)
                    .registerClientboundPacket(ClientboundPlayerPositionPacket.class, ClientboundPlayerPositionPacket::new)
                    .registerClientboundPacket(ClientboundRecipePacket.class, ClientboundRecipePacket::new)
                    .registerClientboundPacket(ClientboundRemoveEntitiesPacket.class, ClientboundRemoveEntitiesPacket::new)
                    .registerClientboundPacket(ClientboundRemoveMobEffectPacket.class, ClientboundRemoveMobEffectPacket::new)
                    .registerClientboundPacket(ClientboundResetScorePacket.class, ClientboundResetScorePacket::new)
                    .registerClientboundPacket(ClientboundResourcePackPopPacket.class, ClientboundResourcePackPopPacket::new)
                    .registerClientboundPacket(ClientboundResourcePackPushPacket.class, ClientboundResourcePackPushPacket::new)
                    .registerClientboundPacket(ClientboundRespawnPacket.class, ClientboundRespawnPacket::new)
                    .registerClientboundPacket(ClientboundRotateHeadPacket.class, ClientboundRotateHeadPacket::new)
                    .registerClientboundPacket(ClientboundSectionBlocksUpdatePacket.class, ClientboundSectionBlocksUpdatePacket::new)
                    .registerClientboundPacket(ClientboundSelectAdvancementsTabPacket.class, ClientboundSelectAdvancementsTabPacket::new)
                    .registerClientboundPacket(ClientboundServerDataPacket.class, ClientboundServerDataPacket::new)
                    .registerClientboundPacket(ClientboundSetActionBarTextPacket.class, ClientboundSetActionBarTextPacket::new)
                    .registerClientboundPacket(ClientboundSetBorderCenterPacket.class, ClientboundSetBorderCenterPacket::new)
                    .registerClientboundPacket(ClientboundSetBorderLerpSizePacket.class, ClientboundSetBorderLerpSizePacket::new)
                    .registerClientboundPacket(ClientboundSetBorderSizePacket.class, ClientboundSetBorderSizePacket::new)
                    .registerClientboundPacket(ClientboundSetBorderWarningDelayPacket.class, ClientboundSetBorderWarningDelayPacket::new)
                    .registerClientboundPacket(ClientboundSetBorderWarningDistancePacket.class, ClientboundSetBorderWarningDistancePacket::new)
                    .registerClientboundPacket(ClientboundSetCameraPacket.class, ClientboundSetCameraPacket::new)
                    .registerClientboundPacket(ClientboundSetCarriedItemPacket.class, ClientboundSetCarriedItemPacket::new)
                    .registerClientboundPacket(ClientboundSetChunkCacheCenterPacket.class, ClientboundSetChunkCacheCenterPacket::new)
                    .registerClientboundPacket(ClientboundSetChunkCacheRadiusPacket.class, ClientboundSetChunkCacheRadiusPacket::new)
                    .registerClientboundPacket(ClientboundSetDefaultSpawnPositionPacket.class, ClientboundSetDefaultSpawnPositionPacket::new)
                    .registerClientboundPacket(ClientboundSetDisplayObjectivePacket.class, ClientboundSetDisplayObjectivePacket::new)
                    .registerClientboundPacket(ClientboundSetEntityDataPacket.class, ClientboundSetEntityDataPacket::new)
                    .registerClientboundPacket(ClientboundSetEntityLinkPacket.class, ClientboundSetEntityLinkPacket::new)
                    .registerClientboundPacket(ClientboundSetEntityMotionPacket.class, ClientboundSetEntityMotionPacket::new)
                    .registerClientboundPacket(ClientboundSetEquipmentPacket.class, ClientboundSetEquipmentPacket::new)
                    .registerClientboundPacket(ClientboundSetExperiencePacket.class, ClientboundSetExperiencePacket::new)
                    .registerClientboundPacket(ClientboundSetHealthPacket.class, ClientboundSetHealthPacket::new)
                    .registerClientboundPacket(ClientboundSetObjectivePacket.class, ClientboundSetObjectivePacket::new)
                    .registerClientboundPacket(ClientboundSetPassengersPacket.class, ClientboundSetPassengersPacket::new)
                    .registerClientboundPacket(ClientboundSetPlayerTeamPacket.class, ClientboundSetPlayerTeamPacket::new)
                    .registerClientboundPacket(ClientboundSetScorePacket.class, ClientboundSetScorePacket::new)
                    .registerClientboundPacket(ClientboundSetSimulationDistancePacket.class, ClientboundSetSimulationDistancePacket::new)
                    .registerClientboundPacket(ClientboundSetSubtitleTextPacket.class, ClientboundSetSubtitleTextPacket::new)
                    .registerClientboundPacket(ClientboundSetTimePacket.class, ClientboundSetTimePacket::new)
                    .registerClientboundPacket(ClientboundSetTitleTextPacket.class, ClientboundSetTitleTextPacket::new)
                    .registerClientboundPacket(ClientboundSetTitlesAnimationPacket.class, ClientboundSetTitlesAnimationPacket::new)
                    .registerClientboundPacket(ClientboundSoundEntityPacket.class, ClientboundSoundEntityPacket::new)
                    .registerClientboundPacket(ClientboundSoundPacket.class, ClientboundSoundPacket::new)
                    .registerClientboundPacket(ClientboundStartConfigurationPacket.class, ClientboundStartConfigurationPacket::new)
                    .registerClientboundPacket(ClientboundStopSoundPacket.class, ClientboundStopSoundPacket::new)
                    .registerClientboundPacket(ClientboundStoreCookiePacket.class, ClientboundStoreCookiePacket::new)
                    .registerClientboundPacket(ClientboundSystemChatPacket.class, ClientboundSystemChatPacket::new)
                    .registerClientboundPacket(ClientboundTabListPacket.class, ClientboundTabListPacket::new)
                    .registerClientboundPacket(ClientboundTagQueryPacket.class, ClientboundTagQueryPacket::new)
                    .registerClientboundPacket(ClientboundTakeItemEntityPacket.class, ClientboundTakeItemEntityPacket::new)
                    .registerClientboundPacket(ClientboundTeleportEntityPacket.class, ClientboundTeleportEntityPacket::new)
                    .registerClientboundPacket(ClientboundTickingStatePacket.class, ClientboundTickingStatePacket::new)
                    .registerClientboundPacket(ClientboundTickingStepPacket.class, ClientboundTickingStepPacket::new)
                    .registerClientboundPacket(ClientboundTransferPacket.class, ClientboundTransferPacket::new)
                    .registerClientboundPacket(ClientboundUpdateAdvancementsPacket.class, ClientboundUpdateAdvancementsPacket::new)
                    .registerClientboundPacket(ClientboundUpdateAttributesPacket.class, ClientboundUpdateAttributesPacket::new)
                    .registerClientboundPacket(ClientboundUpdateMobEffectPacket.class, ClientboundUpdateMobEffectPacket::new)
                    .registerClientboundPacket(ClientboundUpdateRecipesPacket.class, ClientboundUpdateRecipesPacket::new)
                    .registerClientboundPacket(ClientboundUpdateTagsPacket.class, ClientboundUpdateTagsPacket::new)
                    .registerClientboundPacket(ClientboundProjectilePowerPacket.class, ClientboundProjectilePowerPacket::new)
                    .registerServerboundPacket(ServerboundAcceptTeleportationPacket.class, ServerboundAcceptTeleportationPacket::new)
                    .registerServerboundPacket(ServerboundBlockEntityTagQueryPacket.class, ServerboundBlockEntityTagQueryPacket::new)
                    .registerServerboundPacket(ServerboundChangeDifficultyPacket.class, ServerboundChangeDifficultyPacket::new)
                    .registerServerboundPacket(ServerboundChatAckPacket.class, ServerboundChatAckPacket::new)
                    .registerServerboundPacket(ServerboundChatCommandPacket.class, ServerboundChatCommandPacket::new)
                    .registerServerboundPacket(ServerboundChatCommandSignedPacket.class, ServerboundChatCommandSignedPacket::new)
                    .registerServerboundPacket(ServerboundChatPacket.class, ServerboundChatPacket::new)
                    .registerServerboundPacket(ServerboundChatSessionUpdatePacket.class, ServerboundChatSessionUpdatePacket::new)
                    .registerServerboundPacket(ServerboundChunkBatchReceivedPacket.class, ServerboundChunkBatchReceivedPacket::new)
                    .registerServerboundPacket(ServerboundClientCommandPacket.class, ServerboundClientCommandPacket::new)
                    .registerServerboundPacket(ServerboundClientInformationPacket.class, ServerboundClientInformationPacket::new)
                    .registerServerboundPacket(ServerboundCommandSuggestionPacket.class, ServerboundCommandSuggestionPacket::new)
                    .registerServerboundPacket(ServerboundConfigurationAcknowledgedPacket.class, ServerboundConfigurationAcknowledgedPacket::new)
                    .registerServerboundPacket(ServerboundContainerButtonClickPacket.class, ServerboundContainerButtonClickPacket::new)
                    .registerServerboundPacket(ServerboundContainerClickPacket.class, ServerboundContainerClickPacket::new)
                    .registerServerboundPacket(ServerboundContainerClosePacket.class, ServerboundContainerClosePacket::new)
                    .registerServerboundPacket(ServerboundContainerSlotStateChangedPacket.class, ServerboundContainerSlotStateChangedPacket::new)
                    .registerServerboundPacket(ServerboundCookieResponsePacket.class, ServerboundCookieResponsePacket::new)
                    .registerServerboundPacket(ServerboundCustomPayloadPacket.class, ServerboundCustomPayloadPacket::new)
                    .registerServerboundPacket(ServerboundDebugSampleSubscriptionPacket.class, ServerboundDebugSampleSubscriptionPacket::new)
                    .registerServerboundPacket(ServerboundEditBookPacket.class, ServerboundEditBookPacket::new)
                    .registerServerboundPacket(ServerboundEntityTagQuery.class, ServerboundEntityTagQuery::new)
                    .registerServerboundPacket(ServerboundInteractPacket.class, ServerboundInteractPacket::new)
                    .registerServerboundPacket(ServerboundJigsawGeneratePacket.class, ServerboundJigsawGeneratePacket::new)
                    .registerServerboundPacket(ServerboundKeepAlivePacket.class, ServerboundKeepAlivePacket::new)
                    .registerServerboundPacket(ServerboundLockDifficultyPacket.class, ServerboundLockDifficultyPacket::new)
                    .registerServerboundPacket(ServerboundMovePlayerPosPacket.class, ServerboundMovePlayerPosPacket::new)
                    .registerServerboundPacket(ServerboundMovePlayerPosRotPacket.class, ServerboundMovePlayerPosRotPacket::new)
                    .registerServerboundPacket(ServerboundMovePlayerRotPacket.class, ServerboundMovePlayerRotPacket::new)
                    .registerServerboundPacket(ServerboundMovePlayerStatusOnlyPacket.class, ServerboundMovePlayerStatusOnlyPacket::new)
                    .registerServerboundPacket(ServerboundMoveVehiclePacket.class, ServerboundMoveVehiclePacket::new)
                    .registerServerboundPacket(ServerboundPaddleBoatPacket.class, ServerboundPaddleBoatPacket::new)
                    .registerServerboundPacket(ServerboundPickItemPacket.class, ServerboundPickItemPacket::new)
                    .registerServerboundPacket(ServerboundPingRequestPacket.class, ServerboundPingRequestPacket::new)
                    .registerServerboundPacket(ServerboundPlaceRecipePacket.class, ServerboundPlaceRecipePacket::new)
                    .registerServerboundPacket(ServerboundPlayerAbilitiesPacket.class, ServerboundPlayerAbilitiesPacket::new)
                    .registerServerboundPacket(ServerboundPlayerActionPacket.class, ServerboundPlayerActionPacket::new)
                    .registerServerboundPacket(ServerboundPlayerCommandPacket.class, ServerboundPlayerCommandPacket::new)
                    .registerServerboundPacket(ServerboundPlayerInputPacket.class, ServerboundPlayerInputPacket::new)
                    .registerServerboundPacket(ServerboundPongPacket.class, ServerboundPongPacket::new)
                    .registerServerboundPacket(ServerboundRecipeBookChangeSettingsPacket.class, ServerboundRecipeBookChangeSettingsPacket::new)
                    .registerServerboundPacket(ServerboundRecipeBookSeenRecipePacket.class, ServerboundRecipeBookSeenRecipePacket::new)
                    .registerServerboundPacket(ServerboundRenameItemPacket.class, ServerboundRenameItemPacket::new)
                    .registerServerboundPacket(ServerboundResourcePackPacket.class, ServerboundResourcePackPacket::new)
                    .registerServerboundPacket(ServerboundSeenAdvancementsPacket.class, ServerboundSeenAdvancementsPacket::new)
                    .registerServerboundPacket(ServerboundSelectTradePacket.class, ServerboundSelectTradePacket::new)
                    .registerServerboundPacket(ServerboundSetBeaconPacket.class, ServerboundSetBeaconPacket::new)
                    .registerServerboundPacket(ServerboundSetCarriedItemPacket.class, ServerboundSetCarriedItemPacket::new)
                    .registerServerboundPacket(ServerboundSetCommandBlockPacket.class, ServerboundSetCommandBlockPacket::new)
                    .registerServerboundPacket(ServerboundSetCommandMinecartPacket.class, ServerboundSetCommandMinecartPacket::new)
                    .registerServerboundPacket(ServerboundSetCreativeModeSlotPacket.class, ServerboundSetCreativeModeSlotPacket::new)
                    .registerServerboundPacket(ServerboundSetJigsawBlockPacket.class, ServerboundSetJigsawBlockPacket::new)
                    .registerServerboundPacket(ServerboundSetStructureBlockPacket.class, ServerboundSetStructureBlockPacket::new)
                    .registerServerboundPacket(ServerboundSignUpdatePacket.class, ServerboundSignUpdatePacket::new)
                    .registerServerboundPacket(ServerboundSwingPacket.class, ServerboundSwingPacket::new)
                    .registerServerboundPacket(ServerboundTeleportToEntityPacket.class, ServerboundTeleportToEntityPacket::new)
                    .registerServerboundPacket(ServerboundUseItemOnPacket.class, ServerboundUseItemOnPacket::new)
                    .registerServerboundPacket(ServerboundUseItemPacket.class, ServerboundUseItemPacket::new)
            )
            .build();
}
