package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.advancement.Advancement;
import org.geysermc.mcprotocollib.protocol.data.game.advancement.Advancement.DisplayData;
import org.geysermc.mcprotocollib.protocol.data.game.advancement.Advancement.DisplayData.AdvancementType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@With
@AllArgsConstructor
@ToString(exclude = {"advancements", "removedAdvancements", "progress"})
public class ClientboundUpdateAdvancementsPacket implements MinecraftPacket {
    private static final int FLAG_HAS_BACKGROUND_TEXTURE = 0x01;
    private static final int FLAG_SHOW_TOAST = 0x02;
    private static final int FLAG_HIDDEN = 0x04;

    private final boolean reset;
    private final @NonNull Advancement[] advancements;
    private final @NonNull String[] removedAdvancements;
    private final @NonNull Map<String, Map<String, Long>> progress;
    private final boolean showAdvancements;

    public Map<String, Long> getProgress(@NonNull String advancementId) {
        return this.progress.get(advancementId);
    }

    public long getAchievedDate(@NonNull String advancementId, @NonNull String criterionId) {
        Map<String, Long> progress = this.getProgress(advancementId);
        if (progress == null || !progress.containsKey(criterionId)) {
            return -1;
        }

        return progress.get(criterionId);
    }

    public ClientboundUpdateAdvancementsPacket(ByteBuf in) {
        this.reset = in.readBoolean();

        this.advancements = new Advancement[MinecraftTypes.readVarInt(in)];
        for (int i = 0; i < this.advancements.length; i++) {
            String id = MinecraftTypes.readString(in);
            String parentId = MinecraftTypes.readNullable(in, MinecraftTypes::readString);
            DisplayData displayData = null;
            if (in.readBoolean()) {
                Component title = MinecraftTypes.readComponent(in);
                Component description = MinecraftTypes.readComponent(in);
                ItemStack icon = MinecraftTypes.readItemStackTemplate(in);
                AdvancementType advancementType = AdvancementType.from(MinecraftTypes.readVarInt(in));

                int flags = in.readInt();
                boolean hasBackgroundTexture = (flags & FLAG_HAS_BACKGROUND_TEXTURE) != 0;
                boolean showToast = (flags & FLAG_SHOW_TOAST) != 0;
                boolean hidden = (flags & FLAG_HIDDEN) != 0;

                String backgroundTexture = hasBackgroundTexture ? MinecraftTypes.readString(in) : null;
                float posX = in.readFloat();
                float posY = in.readFloat();

                displayData = new DisplayData(title, description, icon, advancementType, showToast, hidden, posX, posY, backgroundTexture);
            }

            int requirementCount = MinecraftTypes.readVarInt(in);
            List<List<String>> requirements = new ArrayList<>(requirementCount);
            for (int j = 0; j < requirementCount; j++) {
                int componentCount = MinecraftTypes.readVarInt(in);
                List<String> requirement = new ArrayList<>(componentCount);
                for (int k = 0; k < componentCount; k++) {
                    requirement.add(MinecraftTypes.readString(in));
                }

                requirements.add(requirement);
            }

            boolean sendTelemetryEvent = in.readBoolean();

            this.advancements[i] = new Advancement(id, requirements, parentId, displayData, sendTelemetryEvent);
        }

        this.removedAdvancements = new String[MinecraftTypes.readVarInt(in)];
        for (int i = 0; i < this.removedAdvancements.length; i++) {
            this.removedAdvancements[i] = MinecraftTypes.readString(in);
        }
        int progressCount = MinecraftTypes.readVarInt(in);
        this.progress = new HashMap<>(progressCount);
        for (int i = 0; i < progressCount; i++) {
            String advancementId = MinecraftTypes.readString(in);
            int criterionCount = MinecraftTypes.readVarInt(in);
            Map<String, Long> advancementProgress = new HashMap<>(criterionCount);
            for (int j = 0; j < criterionCount; j++) {
                String criterionId = MinecraftTypes.readString(in);
                long achievedDate = in.readBoolean() ? in.readLong() : -1;
                advancementProgress.put(criterionId, achievedDate);
            }

            this.progress.put(advancementId, advancementProgress);
        }

        this.showAdvancements = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeBoolean(this.reset);

        MinecraftTypes.writeVarInt(out, this.advancements.length);
        for (int i = 0; i < this.advancements.length; i++) {
            Advancement advancement = this.advancements[i];
            MinecraftTypes.writeString(out, advancement.getId());
            if (advancement.getParentId() != null) {
                out.writeBoolean(true);
                MinecraftTypes.writeString(out, advancement.getParentId());
            } else {
                out.writeBoolean(false);
            }

            DisplayData displayData = advancement.getDisplayData();
            if (displayData != null) {
                out.writeBoolean(true);
                MinecraftTypes.writeComponent(out, displayData.getTitle());
                MinecraftTypes.writeComponent(out, displayData.getDescription());
                MinecraftTypes.writeItemStackTemplate(out, displayData.getIcon());
                MinecraftTypes.writeVarInt(out, displayData.getAdvancementType().ordinal());
                String backgroundTexture = displayData.getBackgroundTexture();

                int flags = 0;
                if (backgroundTexture != null) {
                    flags |= FLAG_HAS_BACKGROUND_TEXTURE;
                }

                if (displayData.isShowToast()) {
                    flags |= FLAG_SHOW_TOAST;
                }

                if (displayData.isHidden()) {
                    flags |= FLAG_HIDDEN;
                }

                out.writeInt(flags);

                if (backgroundTexture != null) {
                    MinecraftTypes.writeString(out, backgroundTexture);
                }

                out.writeFloat(displayData.getPosX());
                out.writeFloat(displayData.getPosY());
            } else {
                out.writeBoolean(false);
            }

            MinecraftTypes.writeVarInt(out, advancement.getRequirements().size());
            for (int j = 0; j < advancement.getRequirements().size(); j++) {
                List<String> requirement = advancement.getRequirements().get(j);
                MinecraftTypes.writeVarInt(out, requirement.size());
                for (int k = 0; k < requirement.size(); k++) {
                    MinecraftTypes.writeString(out, requirement.get(k));
                }
            }

            out.writeBoolean(advancement.isSendsTelemetryEvent());
        }

        MinecraftTypes.writeVarInt(out, this.removedAdvancements.length);
        for (int i = 0; i < this.removedAdvancements.length; i++) {
            MinecraftTypes.writeString(out, this.removedAdvancements[i]);
        }

        MinecraftTypes.writeVarInt(out, this.progress.size());
        for (Map.Entry<String, Map<String, Long>> advancement : this.progress.entrySet()) {
            MinecraftTypes.writeString(out, advancement.getKey());
            Map<String, Long> advancementProgress = advancement.getValue();
            MinecraftTypes.writeVarInt(out, advancementProgress.size());
            for (Map.Entry<String, Long> criterion : advancementProgress.entrySet()) {
                MinecraftTypes.writeString(out, criterion.getKey());
                if (criterion.getValue() != -1) {
                    out.writeBoolean(true);
                    out.writeLong(criterion.getValue());
                } else {
                    out.writeBoolean(false);
                }
            }
        }

        out.writeBoolean(this.showAdvancements);
    }
}
