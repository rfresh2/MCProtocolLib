package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.HolderSet;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.display.RecipeDisplay;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.display.RecipeDisplayEntry;

import java.util.List;

@Data
@With
@AllArgsConstructor
@ToString(exclude = "entries")
public class ClientboundRecipeBookAddPacket implements MinecraftPacket {
    private final List<Entry> entries;
    private final boolean replace;

    public ClientboundRecipeBookAddPacket(ByteBuf in) {
        this.entries = MinecraftTypes.readList(in, buf -> {
            int id = MinecraftTypes.readVarInt(buf);
            RecipeDisplay display = MinecraftTypes.readRecipeDisplay(buf);

            var group = MinecraftTypes.readOptionalVarInt(buf);
            int category = MinecraftTypes.readVarInt(buf);
            List<HolderSet> craftingRequirements = MinecraftTypes.readNullable(in, buf1 -> MinecraftTypes.readList(buf1, MinecraftTypes::readHolderSet));

            byte flags = buf.readByte();
            boolean notification = (flags & 1) != 0;
            boolean highlight = (flags & 2) != 0;
            return new Entry(new RecipeDisplayEntry(id, display, group, category, craftingRequirements), notification, highlight);
        });
        this.replace = in.readBoolean();
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeList(out, this.entries, (buf, entry) -> {
            MinecraftTypes.writeVarInt(buf, entry.contents().id());
            MinecraftTypes.writeRecipeDisplay(buf, entry.contents().display());
            MinecraftTypes.writeOptionalVarInt(buf, entry.contents().group());
            MinecraftTypes.writeVarInt(buf, entry.contents().category());
            MinecraftTypes.writeNullable(buf, entry.contents().craftingRequirements(), (buf1, reqs) -> MinecraftTypes.writeList(buf1, reqs, MinecraftTypes::writeHolderSet));

            int flags = 0;
            if (entry.notification()) {
                flags |= 0x1;
            }

            if (entry.highlight()) {
                flags |= 0x2;
            }
            buf.writeByte(flags);
        });
        out.writeBoolean(this.replace);
    }

    public record Entry(RecipeDisplayEntry contents, boolean notification, boolean highlight) {
    }
}
