package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound;

import com.viaversion.nbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.Holder;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "dialog")
public class ClientboundShowDialogGamePacket implements MinecraftPacket {
    private final Holder<MNBT> dialog;

    public ClientboundShowDialogGamePacket(ByteBuf in) {
        this.dialog = MinecraftTypes.readHolder(in, MinecraftTypes::readMNBT);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeHolder(out, this.dialog, MinecraftTypes::writeMNBT);
    }
}
