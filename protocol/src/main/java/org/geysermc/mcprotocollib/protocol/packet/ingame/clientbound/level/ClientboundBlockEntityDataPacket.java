package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.viaversion.nbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

@Data
@With
@AllArgsConstructor
public class ClientboundBlockEntityDataPacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final @NonNull BlockEntityType type;
    private final @Nullable MNBT nbt;

    public ClientboundBlockEntityDataPacket(ByteBuf in) {
        var position = in.readLong();
        this.x = MinecraftTypes.decodePositionX(position);
        this.y = MinecraftTypes.decodePositionY(position);
        this.z = MinecraftTypes.decodePositionZ(position);
        this.type = MinecraftTypes.readBlockEntityType(in);
        this.nbt = MinecraftTypes.readMNBT(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
        MinecraftTypes.writeBlockEntityType(out, this.type);
        MinecraftTypes.writeMNBT(out, this.nbt);
    }
}
