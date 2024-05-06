package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.github.steveice10.opennbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
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

    public ClientboundBlockEntityDataPacket(ByteBuf in, MinecraftCodecHelper helper) {
        var position = in.readLong();
        this.x = helper.decodePositionX(position);
        this.y = helper.decodePositionY(position);
        this.z = helper.decodePositionZ(position);
        this.type = helper.readBlockEntityType(in);
        this.nbt = helper.readMNBT(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writePosition(out, this.x, this.y, this.z);
        helper.writeBlockEntityType(out, this.type);
        helper.writeMNBT(out, this.nbt);
    }
}
