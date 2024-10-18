package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ClientboundPlayerRotationPacket implements MinecraftPacket {
    private final float yaw;
    private final float pitch;

    public ClientboundPlayerRotationPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        out.writeFloat(this.yaw);
        out.writeFloat(this.pitch);
    }
}