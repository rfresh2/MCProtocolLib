package org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound;

import com.viaversion.nbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "dialog")
public class ClientboundShowDialogConfigurationPacket implements MinecraftPacket {
    private final MNBT dialog;

    public ClientboundShowDialogConfigurationPacket(ByteBuf in) {
        this.dialog = MinecraftTypes.readMNBT(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeMNBT(out, this.dialog);
    }
}
