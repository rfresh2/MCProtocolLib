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
    private final double posX;
    private final double posY;
    private final double posZ;
    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;
    private final float yRot;
    private final float xRot;
    private final boolean onGround;

    public ClientboundEntityPositionSyncPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.id = helper.readVarInt(in);
        this.posX = in.readDouble();
        this.posY = in.readDouble();
        this.posZ = in.readDouble();
        this.deltaX = in.readDouble();
        this.deltaY = in.readDouble();
        this.deltaZ = in.readDouble();
        this.yRot = in.readFloat();
        this.xRot = in.readFloat();
        this.onGround = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.id);
        out.writeDouble(this.posX);
        out.writeDouble(this.posY);
        out.writeDouble(this.posZ);
        out.writeDouble(this.deltaX);
        out.writeDouble(this.deltaY);
        out.writeDouble(this.deltaZ);
        out.writeFloat(this.yRot);
        out.writeFloat(this.xRot);
        out.writeBoolean(this.onGround);
    }
}
