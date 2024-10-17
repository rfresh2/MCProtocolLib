package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ClientboundEntityPositionSyncPacket implements MinecraftPacket {
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;
    private final float yaw;
    private final float pitch;
    private final boolean onGround;

    public ClientboundEntityPositionSyncPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.id = helper.readVarInt(in);
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.deltaX = in.readDouble();
        this.deltaY = in.readDouble();
        this.deltaZ = in.readDouble();
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();
        this.onGround = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.id);
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeDouble(this.deltaX);
        out.writeDouble(this.deltaY);
        out.writeDouble(this.deltaZ);
        out.writeFloat(this.yaw);
        out.writeFloat(this.pitch);
        out.writeBoolean(this.onGround);
    }
}
