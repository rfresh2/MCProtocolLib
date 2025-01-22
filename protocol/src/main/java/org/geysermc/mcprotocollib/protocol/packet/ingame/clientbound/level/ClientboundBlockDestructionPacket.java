package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.BlockBreakStage;

@Data
@With
@AllArgsConstructor
public class ClientboundBlockDestructionPacket implements MinecraftPacket {
    private final int breakerEntityId;
    private final int x;
    private final int y;
    private final int z;
    private final @NonNull BlockBreakStage stage;

    public ClientboundBlockDestructionPacket(ByteBuf in) {
        this.breakerEntityId = MinecraftTypes.readVarInt(in);
        var position = in.readLong();
        this.x = MinecraftTypes.decodePositionX(position);
        this.y = MinecraftTypes.decodePositionY(position);
        this.z = MinecraftTypes.decodePositionZ(position);
        this.stage = MinecraftTypes.readBlockBreakStage(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.breakerEntityId);
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
        MinecraftTypes.writeBlockBreakStage(out, this.stage);
    }
}
