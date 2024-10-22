package org.geysermc.mcprotocollib.protocol.packet.common.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.HandPreference;
import org.geysermc.mcprotocollib.protocol.data.game.setting.ChatVisibility;
import org.geysermc.mcprotocollib.protocol.data.game.setting.ParticleStatus;
import org.geysermc.mcprotocollib.protocol.data.game.setting.SkinPart;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ServerboundClientInformationPacket implements MinecraftPacket {
    private final @NonNull String language;
    private final int viewDistance;
    private final @NonNull ChatVisibility chatVisibility;
    private final boolean chatColors;
    private final @NonNull List<SkinPart> visibleParts;
    private final @NonNull HandPreference mainHand;
    private final boolean textFilteringEnabled;
    private final boolean allowsListing;
    private final @NonNull ParticleStatus particleStatus;

    public ServerboundClientInformationPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.language = helper.readString(in);
        this.viewDistance = in.readByte();
        this.chatVisibility = ChatVisibility.from(helper.readVarInt(in));
        this.chatColors = in.readBoolean();
        this.visibleParts = new ArrayList<>(SkinPart.VALUES.length);

        int flags = in.readUnsignedByte();
        for (SkinPart part : SkinPart.VALUES) {
            int bit = 1 << part.ordinal();
            if ((flags & bit) == bit) {
                this.visibleParts.add(part);
            }
        }

        this.mainHand = HandPreference.from(helper.readVarInt(in));
        this.textFilteringEnabled = in.readBoolean();
        this.allowsListing = in.readBoolean();
        this.particleStatus = ParticleStatus.from(helper.readVarInt(in));
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeString(out, this.language);
        out.writeByte(this.viewDistance);
        helper.writeVarInt(out, this.chatVisibility.ordinal());
        out.writeBoolean(this.chatColors);

        int flags = 0;
        for (SkinPart part : this.visibleParts) {
            flags |= 1 << part.ordinal();
        }

        out.writeByte(flags);

        helper.writeVarInt(out, this.mainHand.ordinal());
        out.writeBoolean(this.textFilteringEnabled);
        out.writeBoolean(allowsListing);
        helper.writeVarInt(out, this.particleStatus.ordinal());
    }
}
