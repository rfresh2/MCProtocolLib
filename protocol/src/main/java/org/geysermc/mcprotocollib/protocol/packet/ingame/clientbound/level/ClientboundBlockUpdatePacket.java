package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;

@Data
@With
@AllArgsConstructor
public class ClientboundBlockUpdatePacket implements MinecraftPacket {
    private final @NonNull BlockChangeEntry entry;

    public ClientboundBlockUpdatePacket(ByteBuf in, MinecraftCodecHelper helper) {
        var position = in.readLong();
        int x = helper.decodePositionX(position);
        int y = helper.decodePositionY(position);
        int z = helper.decodePositionZ(position);
        this.entry = new BlockChangeEntry(x, y, z, helper.readVarInt(in));
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writePosition(out, this.entry.getX(), this.entry.getY(), this.entry.getZ());
        helper.writeVarInt(out, this.entry.getBlock());
    }
}
