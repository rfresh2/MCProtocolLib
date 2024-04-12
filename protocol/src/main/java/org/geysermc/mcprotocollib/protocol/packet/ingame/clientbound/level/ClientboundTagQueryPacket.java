package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;

@Data
@With
@AllArgsConstructor
public class ClientboundTagQueryPacket implements MinecraftPacket {
    private final int transactionId;
    private final @Nullable CompoundTag nbt;

    public ClientboundTagQueryPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.transactionId = helper.readVarInt(in);
        this.nbt = helper.readTag(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.transactionId);
        helper.writeTag(out, this.nbt);
    }
}
