package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ServerboundEditBookPacket implements MinecraftPacket {
    private final int slot;
    private final List<String> pages;
    private final @Nullable String title;

    public ServerboundEditBookPacket(ByteBuf in) {
        this.slot = MinecraftTypes.readVarInt(in);
        int pagesSize = MinecraftTypes.readVarInt(in);
        this.pages = new ArrayList<>(pagesSize);
        for (int i = 0; i < pagesSize; i++) {
            this.pages.add(MinecraftTypes.readString(in));
        }
        if (in.readBoolean()) {
            this.title = MinecraftTypes.readString(in);
        } else {
            this.title = null;
        }
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeVarInt(out, slot);
        MinecraftTypes.writeVarInt(out, this.pages.size());
        for (int i = 0; i < this.pages.size(); i++) {
            MinecraftTypes.writeString(out, this.pages.get(i));
        }
        out.writeBoolean(this.title != null);
        if (this.title != null) {
            MinecraftTypes.writeString(out, title);
        }
    }
}
