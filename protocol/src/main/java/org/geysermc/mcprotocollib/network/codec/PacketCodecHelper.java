package org.geysermc.mcprotocollib.network.codec;

import io.netty.buffer.ByteBuf;

public interface PacketCodecHelper {

    void writeVarInt(ByteBuf buf, int value);

    int readVarInt(ByteBuf buf);

    void write21BitVarInt(ByteBuf buf, int value);

    void writeVarLong(ByteBuf buf, long value);

    long readVarLong(ByteBuf buf);

    String readString(ByteBuf buf);

    String readString(ByteBuf buf, int maxLength);

    void writeString(ByteBuf buf, String value);
}
