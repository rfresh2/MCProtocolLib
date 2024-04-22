package com.github.steveice10.mc.protocol.packet.configuration.clientbound;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.data.game.RegistryEntry;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "entries")
public class ClientboundRegistryDataPacket implements MinecraftPacket {
    private final String registry;
    private final List<RegistryEntry> entries;

    public ClientboundRegistryDataPacket(ByteBuf in, MinecraftCodecHelper helper) throws IOException {
        this.registry = helper.readResourceLocation(in);
        this.entries = new ArrayList<>();

        int entryCount = helper.readVarInt(in);
        for (int i = 0; i < entryCount; i++) {
            this.entries.add(new RegistryEntry(helper.readResourceLocation(in), helper.readNullable(in, helper::readTag)));
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) throws IOException {
        helper.writeResourceLocation(out, this.registry);

        helper.writeVarInt(out, this.entries.size());
        for (RegistryEntry entry : this.entries) {
            helper.writeResourceLocation(out, entry.getId());
            helper.writeNullable(out, entry.getData(), helper::writeTag);
        }
    }
}
