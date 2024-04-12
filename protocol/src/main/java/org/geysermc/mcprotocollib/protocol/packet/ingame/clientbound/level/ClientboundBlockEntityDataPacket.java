package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

@Data
@With
@AllArgsConstructor
public class ClientboundBlockEntityDataPacket implements MinecraftPacket {
    private final @NonNull Vector3i position;
    private final @NonNull BlockEntityType type;
    private final @Nullable CompoundTag nbt;

    public ClientboundBlockEntityDataPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.position = helper.readPosition(in);
        this.type = helper.readBlockEntityType(in);
        this.nbt = helper.readTag(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writePosition(out, this.position);
        helper.writeBlockEntityType(out, this.type);
        helper.writeTag(out, this.nbt);
    }
}
