package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
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
    private final int sequence;

    public ServerboundUseItemOnPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.hand = Hand.from(helper.readVarInt(in));
        var position = in.readLong();
        this.x = helper.decodePositionX(position);
        this.y = helper.decodePositionY(position);
        this.z = helper.decodePositionZ(position);
        this.face = helper.readDirection(in);
        this.cursorX = in.readFloat();
        this.cursorY = in.readFloat();
        this.cursorZ = in.readFloat();
        this.insideBlock = in.readBoolean();
        this.sequence = helper.readVarInt(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.hand.ordinal());
        helper.writePosition(out, this.x, this.y, this.z);
        helper.writeDirection(out, this.face);
        out.writeFloat(this.cursorX);
        out.writeFloat(this.cursorY);
        out.writeFloat(this.cursorZ);
        out.writeBoolean(this.insideBlock);
        helper.writeVarInt(out, this.sequence);
    }
}
