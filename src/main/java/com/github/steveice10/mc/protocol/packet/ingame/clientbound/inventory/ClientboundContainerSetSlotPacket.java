package com.github.steveice10.mc.protocol.packet.ingame.clientbound.inventory;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.data.game.item.ItemStack;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Data
@With
@AllArgsConstructor
public class ClientboundContainerSetSlotPacket implements MinecraftPacket {
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final @Nullable ItemStack item;

    public ClientboundContainerSetSlotPacket(ByteBuf in, MinecraftCodecHelper helper) throws IOException {
        this.containerId = in.readUnsignedByte();
        this.stateId = helper.readVarInt(in);
        this.slot = in.readShort();
        this.item = helper.readOptionalItemStack(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) throws IOException {
        out.writeByte(this.containerId);
        helper.writeVarInt(out, this.stateId);
        out.writeShort(this.slot);
        helper.writeOptionalItemStack(out, this.item);
    }
}
