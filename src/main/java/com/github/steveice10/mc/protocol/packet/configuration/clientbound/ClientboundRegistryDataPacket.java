package com.github.steveice10.mc.protocol.packet.configuration.clientbound;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;

import java.io.IOException;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "registry")
public class ClientboundRegistryDataPacket implements MinecraftPacket {
    private final CompoundTag registry;

    public ClientboundRegistryDataPacket(ByteBuf in, MinecraftCodecHelper helper) throws IOException {
        this.registry = helper.readTag(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) throws IOException {
        helper.writeTag(out, this.registry);
    }
}
