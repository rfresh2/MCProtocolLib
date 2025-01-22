package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory;

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
public class ClientboundPlaceGhostRecipePacket implements MinecraftPacket {
    private final int containerId;
    private final @NonNull String recipeId;

    public ClientboundPlaceGhostRecipePacket(ByteBuf in) {
        this.containerId = in.readByte();
        this.recipeId = MinecraftTypes.readString(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeByte(this.containerId);
        MinecraftTypes.writeString(out, this.recipeId);
    }
}
