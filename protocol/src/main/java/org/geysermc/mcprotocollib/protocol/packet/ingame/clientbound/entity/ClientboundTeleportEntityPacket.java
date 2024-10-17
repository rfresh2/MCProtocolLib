package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PositionElement;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundTeleportEntityPacket implements MinecraftPacket {
    private final int id;
    private final double posX;
    private final double posY;
    private final double posZ;
    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;
    private final float yRot;
    private final float xRot;
    private final @NonNull List<PositionElement> relatives;
    private final boolean onGround;

    public ClientboundTeleportEntityPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.id = helper.readVarInt(in);
        this.posX = in.readDouble();
        this.posY = in.readDouble();
        this.posZ = in.readDouble();
        this.deltaX = in.readDouble();
        this.deltaY = in.readDouble();
        this.deltaZ = in.readDouble();
        this.yRot = in.readFloat();
        this.xRot = in.readFloat();

        this.relatives = new ArrayList<>(PositionElement.values().length);
        int flags = in.readInt();
        for (PositionElement element : PositionElement.values()) {
            int bit = 1 << element.ordinal();
            if ((flags & bit) == bit) {
                this.relatives.add(element);
            }
        }

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

        int flags = 0;
        for (PositionElement element : this.relatives) {
            flags |= 1 << element.ordinal();
        }
        out.writeInt(flags);

        out.writeBoolean(this.onGround);
    }
}
