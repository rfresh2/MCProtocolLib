package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

@Data
@With
@AllArgsConstructor
public class ServerboundChatPacket implements MinecraftPacket {
    private @NotNull String message;
    private long timeStamp;
    private long salt;
    private byte @Nullable [] signature;
    private int offset;
    private BitSet acknowledgedMessages;
    private int checksum;

    // default unsigned chat packet
    public ServerboundChatPacket(final String message) {
        this.message = message;
        this.timeStamp = System.currentTimeMillis();
        this.salt = 0;
        this.signature = null;
        this.offset = 0;
        this.acknowledgedMessages = BitSet.valueOf(new byte[20]);
        this.checksum = 0;
    }

    public ServerboundChatPacket(ByteBuf in) {
        this.message = MinecraftTypes.readString(in);
        this.timeStamp = in.readLong();
        this.salt = in.readLong();
        this.signature = MinecraftTypes.readNullable(in, buf -> {
            byte[] signature = new byte[256];
            buf.readBytes(signature);
            return signature;
        });

        this.offset = MinecraftTypes.readVarInt(in);
        this.acknowledgedMessages = MinecraftTypes.readFixedBitSet(in, 20);
        this.checksum = in.readByte();
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeString(out, this.message);
        out.writeLong(this.timeStamp);
        out.writeLong(this.salt);
        MinecraftTypes.writeNullable(out, this.signature, ByteBuf::writeBytes);

        MinecraftTypes.writeVarInt(out, this.offset);
        MinecraftTypes.writeFixedBitSet(out, this.acknowledgedMessages, 20);
        out.writeByte(this.checksum);
    }
}
