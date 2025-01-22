package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

@Data
@With
@AllArgsConstructor
public class ServerboundUseItemOnPacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final @NonNull Direction face;
    private final @NonNull Hand hand;
    private final float cursorX;
    private final float cursorY;
    private final float cursorZ;
    private final boolean insideBlock;
    private final boolean hitWorldBorder;
    private final int sequence;

    public ServerboundUseItemOnPacket(ByteBuf in) {
        this.hand = Hand.from(MinecraftTypes.readVarInt(in));
        var position = in.readLong();
        this.x = MinecraftTypes.decodePositionX(position);
        this.y = MinecraftTypes.decodePositionY(position);
        this.z = MinecraftTypes.decodePositionZ(position);
        this.face = MinecraftTypes.readDirection(in);
        this.cursorX = in.readFloat();
        this.cursorY = in.readFloat();
        this.cursorZ = in.readFloat();
        this.insideBlock = in.readBoolean();
        this.hitWorldBorder = in.readBoolean();
        this.sequence = MinecraftTypes.readVarInt(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.hand.ordinal());
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
        MinecraftTypes.writeDirection(out, this.face);
        out.writeFloat(this.cursorX);
        out.writeFloat(this.cursorY);
        out.writeFloat(this.cursorZ);
        out.writeBoolean(this.insideBlock);
        out.writeBoolean(this.hitWorldBorder);
        MinecraftTypes.writeVarInt(out, this.sequence);
    }
}
