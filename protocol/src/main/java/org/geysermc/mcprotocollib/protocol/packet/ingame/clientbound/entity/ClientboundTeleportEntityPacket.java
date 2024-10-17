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
import java.util.Collections;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundTeleportEntityPacket implements MinecraftPacket {
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;
    private final float yaw;
    private final float pitch;
    private final @NonNull List<PositionElement> relatives;
    private final boolean onGround;

    public ClientboundTeleportEntityPacket(int id, double x, double y, double z, double deltaX, double deltaY, double deltaZ, float yaw, float pitch, boolean onGround) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.relatives = Collections.emptyList();
        this.onGround = onGround;
    }

    public ClientboundTeleportEntityPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.id = helper.readVarInt(in);
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.deltaX = in.readDouble();
        this.deltaY = in.readDouble();
        this.deltaZ = in.readDouble();
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();

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
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeDouble(this.deltaX);
        out.writeDouble(this.deltaY);
        out.writeDouble(this.deltaZ);
        out.writeFloat(this.yaw);
        out.writeFloat(this.pitch);

        int flags = 0;
        for (PositionElement element : this.relatives) {
            flags |= 1 << element.ordinal();
        }
        out.writeInt(flags);

        out.writeBoolean(this.onGround);
    }
}
