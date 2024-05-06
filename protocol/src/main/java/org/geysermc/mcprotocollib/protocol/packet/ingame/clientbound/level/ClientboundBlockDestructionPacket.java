package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
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

    public ClientboundBlockDestructionPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.breakerEntityId = helper.readVarInt(in);
        var position = in.readLong();
        this.x = helper.decodePositionX(position);
        this.y = helper.decodePositionY(position);
        this.z = helper.decodePositionZ(position);
        this.stage = helper.readBlockBreakStage(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.breakerEntityId);
        helper.writePosition(out, this.x, this.y, this.z);
        helper.writeBlockBreakStage(out, this.stage);
    }
}
