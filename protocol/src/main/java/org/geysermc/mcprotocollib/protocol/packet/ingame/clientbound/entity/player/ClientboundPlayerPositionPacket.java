package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player;

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
public class ClientboundPlayerPositionPacket implements MinecraftPacket {
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

    public ClientboundPlayerPositionPacket(ByteBuf in, MinecraftCodecHelper helper) {
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
        for (int i = 0; i < this.relatives.size(); i++) {
            flags |= 1 << this.relatives.get(i).ordinal();
        }

        out.writeInt(flags);
    }
}
