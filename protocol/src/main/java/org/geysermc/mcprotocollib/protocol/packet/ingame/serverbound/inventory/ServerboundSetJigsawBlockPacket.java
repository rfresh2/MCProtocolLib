package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ServerboundSetJigsawBlockPacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final @NonNull String name;
    private final @NonNull String target;
    private final @NonNull String pool;
    private final @NonNull String finalState;
    private final @NonNull String jointType;
    private final int selectionPriority;
    private final int placementPriority;

    public ServerboundSetJigsawBlockPacket(ByteBuf in, MinecraftCodecHelper helper) {
        var position = in.readLong();
        this.x = helper.decodePositionX(position);
        this.y = helper.decodePositionY(position);
        this.z = helper.decodePositionZ(position);
        this.name = helper.readString(in);
        this.target = helper.readString(in);
        this.pool = helper.readString(in);
        this.finalState = helper.readString(in);
        this.jointType = helper.readString(in);
        this.selectionPriority = helper.readVarInt(in);
        this.placementPriority = helper.readVarInt(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writePosition(out, this.x, this.y, this.z);
        helper.writeString(out, this.name);
        helper.writeString(out, this.target);
        helper.writeString(out, this.pool);
        helper.writeString(out, this.finalState);
        helper.writeString(out, this.jointType);
        helper.writeVarInt(out, this.selectionPriority);
        helper.writeVarInt(out, this.placementPriority);
    }
}
