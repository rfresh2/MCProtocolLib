package org.geysermc.mcprotocollib.protocol.packet.common.serverbound;

import com.viaversion.nbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "payload")
public class ServerboundCustomClickActionPacket implements MinecraftPacket {
    private final Key id;
    private final MNBT payload;

    public ServerboundCustomClickActionPacket(ByteBuf in) {
        this.id = MinecraftTypes.readResourceLocation(in);
        this.payload = MinecraftTypes.readLengthPrefixed(in, 65536, MinecraftTypes::readMNBT);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeResourceLocation(out, this.id);
        MinecraftTypes.writeLengthPrefixed(out, 65536, this.payload, MinecraftTypes::writeMNBT);
        MinecraftTypes.writeVarInt(out, this.payload.getData().length);
        MinecraftTypes.writeMNBT(out, this.payload);
    }
}
