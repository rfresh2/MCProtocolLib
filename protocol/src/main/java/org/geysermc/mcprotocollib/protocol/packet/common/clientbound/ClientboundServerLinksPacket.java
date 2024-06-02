package org.geysermc.mcprotocollib.protocol.packet.common.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.ServerLink;
import org.geysermc.mcprotocollib.protocol.data.game.ServerLinkType;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundServerLinksPacket implements MinecraftPacket {
    private final List<ServerLink> links;

    public ClientboundServerLinksPacket(ByteBuf in, MinecraftCodecHelper helper) {
        int length = helper.readVarInt(in);
        this.links = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            ServerLinkType knownType = null;
            Component unknownType = null;
            if (in.readBoolean()) {
                knownType = ServerLinkType.from(helper.readVarInt(in));
            } else {
                unknownType = helper.readComponent(in);
            }

            String link = helper.readString(in);
            this.links.add(new ServerLink(knownType, unknownType, link));
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.links.size());
        List<ServerLink> serverLinks = this.links;
        for (int i = 0; i < serverLinks.size(); i++) {
            final ServerLink link = serverLinks.get(i);
            out.writeBoolean(link.knownType() != null);
            if (link.knownType() != null) {
                helper.writeVarInt(out, link.knownType().ordinal());
            } else {
                helper.writeComponent(out, link.unknownType());
            }

            helper.writeString(out, link.link());
        }
    }
}
