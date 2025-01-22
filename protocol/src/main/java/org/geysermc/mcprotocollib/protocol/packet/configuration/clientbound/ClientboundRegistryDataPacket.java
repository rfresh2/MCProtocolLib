package org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "entries")
public class ClientboundRegistryDataPacket implements MinecraftPacket {
    private final String registry;
    private final List<RegistryEntry> entries;

    public ClientboundRegistryDataPacket(ByteBuf in) {
        this.registry = MinecraftTypes.readResourceLocationString(in);
        int entryCount = MinecraftTypes.readVarInt(in);
        this.entries = new ArrayList<>(entryCount);

        for (int i = 0; i < entryCount; i++) {
            this.entries.add(new RegistryEntry(MinecraftTypes.readResourceLocationString(in), MinecraftTypes.readNullable(in, MinecraftTypes::readMNBT)));
        }
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeResourceLocation(out, this.registry);

        MinecraftTypes.writeVarInt(out, this.entries.size());
        for (int i = 0; i < this.entries.size(); i++) {
            RegistryEntry entry = this.entries.get(i);
            MinecraftTypes.writeResourceLocation(out, entry.getId());
            MinecraftTypes.writeNullable(out, entry.getData(), MinecraftTypes::writeMNBT);
        }
    }
}
