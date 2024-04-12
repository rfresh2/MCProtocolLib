package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

@Data
@With
@AllArgsConstructor
public class ServerboundChatPacket implements MinecraftPacket {
    private @NotNull String message;
    private long timeStamp;
    private final long salt;
    private final byte @Nullable [] signature;
    private final int offset;
    private final BitSet acknowledgedMessages;


    // default unsigned chat packet
    public ServerboundChatPacket(final String message) {
        this.message = message;
        this.timeStamp = System.currentTimeMillis();
        this.salt = 0;
        this.signature = null;
        this.offset = 0;
        this.acknowledgedMessages = BitSet.valueOf(new byte[20]);
    }

    public ServerboundChatPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.message = helper.readString(in);
        this.timeStamp = in.readLong();
        this.salt = in.readLong();
        if (in.readBoolean()) {
            this.signature = new byte[256];
            in.readBytes(this.signature);
        } else {
            this.signature = null;
        }

        this.offset = helper.readVarInt(in);
        this.acknowledgedMessages = helper.readFixedBitSet(in, 20);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeString(out, this.message);
        out.writeLong(this.timeStamp);
        out.writeLong(this.salt);
        out.writeBoolean(this.signature != null);
        if (this.signature != null) {
            out.writeBytes(this.signature);
        }

        helper.writeVarInt(out, this.offset);
        helper.writeFixedBitSet(out, this.acknowledgedMessages, 20);
    }
}
