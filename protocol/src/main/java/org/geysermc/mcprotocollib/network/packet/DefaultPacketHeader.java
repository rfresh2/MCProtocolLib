package org.geysermc.mcprotocollib.network.packet;

import io.netty.buffer.ByteBuf;
import org.geysermc.mcprotocollib.network.codec.PacketCodecHelper;

/**
 * The default packet header, using a varint packet length and id.
 */
public class DefaultPacketHeader implements PacketHeader {

    private static final int[] VARINT_EXACT_BYTE_LENGTHS = new int[33];

    static {
        for (int i = 0; i <= 32; ++i) {
            VARINT_EXACT_BYTE_LENGTHS[i] = (int) Math.ceil((31d - (i - 1)) / 7d);
        }
        VARINT_EXACT_BYTE_LENGTHS[32] = 1; // Special case for the number 0.
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
        return VARINT_EXACT_BYTE_LENGTHS[Integer.numberOfLeadingZeros(length)];
    }

    @Override
    public int readLength(ByteBuf buf, PacketCodecHelper codecHelper, int available) {
        return codecHelper.readVarInt(buf);
    }

    @Override
    public void writeLength(ByteBuf buf, PacketCodecHelper codecHelper, int length) {
        codecHelper.writeVarInt(buf, length);
    }

    @Override
    public int readPacketId(ByteBuf buf, PacketCodecHelper codecHelper) {
        return codecHelper.readVarInt(buf);
    }

    @Override
    public void writePacketId(ByteBuf buf, PacketCodecHelper codecHelper, int packetId) {
        codecHelper.writeVarInt(buf, packetId);
    }
}
