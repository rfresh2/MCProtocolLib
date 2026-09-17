package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@With
public class ServerboundSignUpdatePacket implements MinecraftPacket {
    private final int x;
    private final int y;
    private final int z;
    private final @NonNull List<String> lines;
    private final boolean isFrontText;

    public ServerboundSignUpdatePacket(int x, int y, int z, @NonNull List<String> lines, boolean isFrontText) {
        if (lines.size() != 4) {
            throw new IllegalArgumentException("Lines must contain exactly 4 strings.");
        }

        this.x = x;
        this.y = y;
        this.z = z;
        this.lines = List.copyOf(lines);
        this.isFrontText = isFrontText;
    }

    public ServerboundSignUpdatePacket(ByteBuf in) {
        var position = in.readLong();
        this.x = MinecraftTypes.decodePositionX(position);
        this.y = MinecraftTypes.decodePositionY(position);
        this.z = MinecraftTypes.decodePositionZ(position);
        this.lines = new ArrayList<>(4);
        for (int count = 0; count < 4; count++) {
            this.lines.add(MinecraftTypes.readString(in, 384));
        }
        this.isFrontText = MinecraftTypes.readVarInt(in) == 1;
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
        for (String line : this.lines) {
            MinecraftTypes.writeString(out, line);
        }
        out.writeBoolean(this.isFrontText);
    }
}
