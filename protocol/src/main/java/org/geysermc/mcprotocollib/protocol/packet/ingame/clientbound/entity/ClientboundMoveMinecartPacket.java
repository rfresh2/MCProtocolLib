package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.entity.MinecartStep;

import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundMoveMinecartPacket implements MinecraftPacket {
    private int entityId;
    private List<MinecartStep> lerpSteps;

    public ClientboundMoveMinecartPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.entityId = helper.readVarInt(in);
        this.lerpSteps = helper.readList(in, (input) -> {
            double posX = input.readDouble();
            double posY = input.readDouble();
            double posZ = input.readDouble();
            double motionX = input.readDouble();
            double motionY = input.readDouble();
            double motionZ = input.readDouble();
            float yRot = input.readByte() * 360F / 256F;
            float xRot = input.readByte() * 360F / 256F;
            float weight = input.readFloat();
            return new MinecartStep(posX, posY, posZ, motionX, motionY, motionZ, yRot, xRot, weight);
        });
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.entityId);
        helper.writeList(out, this.lerpSteps, (output, lerpStep) -> {
            output.writeDouble(lerpStep.x());
            output.writeDouble(lerpStep.y());
            output.writeDouble(lerpStep.z());

            output.writeDouble(lerpStep.motionX());
            output.writeDouble(lerpStep.motionY());
            output.writeDouble(lerpStep.motionZ());

            float yRot = lerpStep.yRot() * 256F / 360F;
            output.writeByte(yRot < (int)yRot ? (int)yRot - 1 : (int)yRot);
            float xRot = lerpStep.xRot() * 256F / 360F;
            output.writeByte(xRot < (int)xRot ? (int)xRot - 1 : (int)xRot);

            output.writeFloat(lerpStep.weight());
        });
    }
}
