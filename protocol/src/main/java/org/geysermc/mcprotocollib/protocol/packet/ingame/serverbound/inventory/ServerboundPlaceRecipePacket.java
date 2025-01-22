package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
public class ServerboundPlaceRecipePacket implements MinecraftPacket {
    private final int containerId;
    private final @NonNull String recipeId;
    private final boolean makeAll;

    public ServerboundPlaceRecipePacket(ByteBuf in) {
        this.containerId = in.readByte();
        this.recipeId = MinecraftTypes.readString(in);
        this.makeAll = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeByte(this.containerId);
        MinecraftTypes.writeString(out, this.recipeId);
        out.writeBoolean(this.makeAll);
    }
}
