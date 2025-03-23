package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.ArgumentSignature;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ServerboundChatCommandSignedPacket implements MinecraftPacket {
    private final String command;
    private long timeStamp;
    private long salt;
    private List<ArgumentSignature> signatures;
    private int offset;
    private BitSet acknowledgedMessages;
    private byte checksum;

    public ServerboundChatCommandSignedPacket(String command) {
        this.command = command;
        this.timeStamp = System.currentTimeMillis();
        this.salt = 0;
        this.signatures = new ArrayList<>(0);
        this.offset = 0;
        this.acknowledgedMessages = new BitSet(20);
        this.checksum = 0;
    }

    public ServerboundChatCommandSignedPacket(ByteBuf in) {
        this.command = MinecraftTypes.readString(in);
        this.timeStamp = in.readLong();
        this.salt = in.readLong();
        int signatureCount = Math.min(MinecraftTypes.readVarInt(in), 8);
        this.signatures = new ArrayList<>(signatureCount);
        for (int i = 0; i < signatureCount; i++) {
            byte[] signature = new byte[256];
            signatures.add(new ArgumentSignature(MinecraftTypes.readString(in, 16), signature));
            in.readBytes(signature);
        }

        this.offset = MinecraftTypes.readVarInt(in);
        this.acknowledgedMessages = MinecraftTypes.readFixedBitSet(in, 20);
        this.checksum = in.readByte();
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writeString(out, this.command);
        out.writeLong(this.timeStamp);
        out.writeLong(this.salt);
        MinecraftTypes.writeVarInt(out, this.signatures.size());
        for (int i = 0; i < this.signatures.size(); i++) {
            ArgumentSignature signature = this.signatures.get(i);
            MinecraftTypes.writeString(out, signature.getName());
            out.writeBytes(signature.getSignature());
        }

        MinecraftTypes.writeVarInt(out, this.offset);
        MinecraftTypes.writeFixedBitSet(out, this.acknowledgedMessages, 20);
        out.writeByte(this.checksum);
    }
}
