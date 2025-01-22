package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@AllArgsConstructor
public class ClientboundDamageEventPacket implements MinecraftPacket {
    private final int entityId;
    private final int sourceTypeId;
    private final int sourceCauseId;
    private final int sourceDirectId;
    private final boolean hasSourcePos;
    private final double sourcePosX;
    private final double sourcePosY;
    private final double sourcePosZ;

    public ClientboundDamageEventPacket(ByteBuf in) {
        this.entityId = MinecraftTypes.readVarInt(in);
        this.sourceTypeId = MinecraftTypes.readVarInt(in);
        this.sourceCauseId = MinecraftTypes.readVarInt(in) - 1;
        this.sourceDirectId = MinecraftTypes.readVarInt(in) - 1;
        this.hasSourcePos = in.readBoolean();
        if (this.hasSourcePos) {
            this.sourcePosX = in.readDouble();
            this.sourcePosY = in.readDouble();
            this.sourcePosZ = in.readDouble();
        } else {
            this.sourcePosX = 0;
            this.sourcePosY = 0;
            this.sourcePosZ = 0;
        }
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.entityId);
        MinecraftTypes.writeVarInt(out, this.sourceTypeId);
        MinecraftTypes.writeVarInt(out, this.sourceCauseId + 1);
        MinecraftTypes.writeVarInt(out, this.sourceDirectId + 1);

        if (this.hasSourcePos) {
            out.writeBoolean(true);
            out.writeDouble(this.sourcePosX);
            out.writeDouble(this.sourcePosY);
            out.writeDouble(this.sourcePosZ);
        } else {
            out.writeBoolean(false);
        }
    }
}
