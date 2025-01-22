package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;

@Data
@With
@AllArgsConstructor
public class ClientboundBlockUpdatePacket implements MinecraftPacket {
    private final @NonNull BlockChangeEntry entry;

    public ClientboundBlockUpdatePacket(ByteBuf in) {
        var position = in.readLong();
        int x = MinecraftTypes.decodePositionX(position);
        int y = MinecraftTypes.decodePositionY(position);
        int z = MinecraftTypes.decodePositionZ(position);
        this.entry = new BlockChangeEntry(x, y, z, MinecraftTypes.readVarInt(in));
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writePosition(out, this.entry.getX(), this.entry.getY(), this.entry.getZ());
        MinecraftTypes.writeVarInt(out, this.entry.getBlock());
    }
}
