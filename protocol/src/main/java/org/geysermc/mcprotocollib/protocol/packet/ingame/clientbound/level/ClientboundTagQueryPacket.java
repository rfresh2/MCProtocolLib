package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.viaversion.nbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
public class ClientboundTagQueryPacket implements MinecraftPacket {
    private final int transactionId;
    private final @Nullable MNBT nbt;

    public ClientboundTagQueryPacket(ByteBuf in) {
        this.transactionId = MinecraftTypes.readVarInt(in);
        this.nbt = MinecraftTypes.readMNBT(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.transactionId);
        MinecraftTypes.writeMNBT(out, this.nbt);
    }
}
