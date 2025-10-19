package org.geysermc.mcprotocollib.network.packet;

import io.netty.buffer.ByteBuf;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

/**
 * The default packet header, using a varint packet length and id.
 */
public class DefaultPacketHeader implements PacketHeader {

    private static final int[] VAR_INT_LENGTHS = new int[33];
    static {
        for (int i = 0; i <= 32; ++i) {
            VAR_INT_LENGTHS[i] = (int) Math.ceil((31d - (i - 1)) / 7d);
        }
        VAR_INT_LENGTHS[32] = 1; // Special case for the number 0.
    }

    @Override
    public boolean isLengthVariable() {
        return true;
    }

    @Override
    public int getLengthSize() {
        return 5;
    }

    @Override
    public int getLengthSize(int length) {
        return VAR_INT_LENGTHS[Integer.numberOfLeadingZeros(length)];
    }

    @Override
    public int readLength(ByteBuf buf, int available) {
        return MinecraftTypes.readVarInt(buf);
    }

    @Override
    public void writeLength(ByteBuf buf, int length) {
        MinecraftTypes.writeVarInt(buf, length);
    }

    @Override
    public int readPacketId(ByteBuf buf) {
        return MinecraftTypes.readVarInt(buf);
    }

    @Override
    public void writePacketId(ByteBuf buf, int packetId) {
        MinecraftTypes.writeVarInt(buf, packetId);
    }
}
