package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
public class ServerboundSetJigsawBlockPacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final @NonNull Key name;
    private final @NonNull Key target;
    private final @NonNull Key pool;
    private final @NonNull String finalState;
    private final @NonNull String jointType;
    private final int selectionPriority;
    private final int placementPriority;

    public ServerboundSetJigsawBlockPacket(ByteBuf in) {
        var position = in.readLong();
        this.x = MinecraftTypes.decodePositionX(position);
        this.y = MinecraftTypes.decodePositionY(position);
        this.z = MinecraftTypes.decodePositionZ(position);
        this.name = MinecraftTypes.readResourceLocation(in);
        this.target = MinecraftTypes.readResourceLocation(in);
        this.pool = MinecraftTypes.readResourceLocation(in);
        this.finalState = MinecraftTypes.readString(in);
        this.jointType = MinecraftTypes.readString(in);
        this.selectionPriority = MinecraftTypes.readVarInt(in);
        this.placementPriority = MinecraftTypes.readVarInt(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
        MinecraftTypes.writeResourceLocation(out, this.name);
        MinecraftTypes.writeResourceLocation(out, this.target);
        MinecraftTypes.writeResourceLocation(out, this.pool);
        MinecraftTypes.writeString(out, this.finalState);
        MinecraftTypes.writeString(out, this.jointType);
        MinecraftTypes.writeVarInt(out, this.selectionPriority);
        MinecraftTypes.writeVarInt(out, this.placementPriority);
    }
}
