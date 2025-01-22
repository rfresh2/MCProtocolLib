package org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

@Data
@With
@AllArgsConstructor
public class ClientboundUpdateEnabledFeaturesPacket implements MinecraftPacket {
    private final String[] features;

    public ClientboundUpdateEnabledFeaturesPacket(ByteBuf in) {
        this.features = new String[MinecraftTypes.readVarInt(in)];
        for (int i = 0; i < this.features.length; i++) {
            this.features[i] = MinecraftTypes.readString(in);
        }
    }

    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, this.features.length);
        for (String feature : this.features) {
            MinecraftTypes.writeString(out, feature);
        }
    }
}
