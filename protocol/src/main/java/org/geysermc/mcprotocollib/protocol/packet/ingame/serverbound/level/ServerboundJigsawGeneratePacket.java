package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ServerboundJigsawGeneratePacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final int levels;
    private final boolean keepJigsaws;

    public ServerboundJigsawGeneratePacket(ByteBuf in, MinecraftCodecHelper helper) {
        var position = in.readLong();
        this.x = helper.decodePositionX(position);
        this.y = helper.decodePositionY(position);
        this.z = helper.decodePositionZ(position);
        this.levels = helper.readVarInt(in);
        this.keepJigsaws = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writePosition(out, this.x, this.y, this.z);
        helper.writeVarInt(out, this.levels);
        out.writeBoolean(this.keepJigsaws);
    }
}
