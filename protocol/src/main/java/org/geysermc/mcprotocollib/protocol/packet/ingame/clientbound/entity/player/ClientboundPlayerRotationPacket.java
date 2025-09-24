package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ClientboundPlayerRotationPacket implements MinecraftPacket {
    private final float yaw;
    private final boolean relativeYaw;
    private final float pitch;
    private final boolean relativePitch;

    public ClientboundPlayerRotationPacket(ByteBuf in) {
        this.yaw = in.readFloat();
        this.relativeYaw = in.readBoolean();
        this.pitch = in.readFloat();
        this.relativePitch = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeFloat(this.yaw);
        out.writeBoolean(this.relativeYaw);
        out.writeFloat(this.pitch);
        out.writeBoolean(this.relativePitch);
    }
}
