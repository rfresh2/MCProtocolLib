package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerAction;

@Data
@With
@AllArgsConstructor
public class ServerboundPlayerActionPacket implements MinecraftPacket {
    private final @NonNull PlayerAction action;
    private final int x;
    private final int y;
    private final int z;
    private final @NonNull Direction face;
    private final int sequence;

    public ServerboundPlayerActionPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.action = PlayerAction.from(helper.readVarInt(in));
        var position = in.readLong();
        this.x = helper.decodePositionX(position);
        this.y = helper.decodePositionY(position);
        this.z = helper.decodePositionZ(position);
        this.face = Direction.VALUES[in.readUnsignedByte()];
        this.sequence = helper.readVarInt(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.action.ordinal());
        helper.writePosition(out, this.x, this.y, this.z);
        out.writeByte(this.face.ordinal());
        helper.writeVarInt(out, this.sequence);
    }
}
