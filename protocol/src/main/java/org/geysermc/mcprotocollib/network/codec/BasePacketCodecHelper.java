package org.geysermc.mcprotocollib.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;

public class BasePacketCodecHelper implements PacketCodecHelper {

    @Override
    public void writeVarInt(ByteBuf buf, int value) {
        // Peel the one and two byte count cases explicitly as they are the most common VarInt sizes
        // that the proxy will write, to improve inlining.
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else {
            writeVarIntFull(buf, value);
        }
    }

    @Override
    public void write21BitVarInt(ByteBuf buf, int value) {
        int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
        buf.writeMedium(w);
    }

    private static void writeVarIntFull(ByteBuf buf, int value) {
        // See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
            buf.writeMedium(w);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21);
            buf.writeInt(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
        }
    }

    @Override
    public int readVarInt(ByteBuf buf) {
        int readable = buf.readableBytes();
        if (readable == 0) {
            // special case for empty buffer
            throw new IllegalArgumentException("VarInt too short (0 size readable buffer)");
        }

        // we can read at least one byte, and this should be a common case
        int k = buf.readByte();
        if ((k & 0x80) != 128) {
            return k;
        }

        // in case decoding one byte was not enough, use a loop to decode up to the next 4 bytes
        int maxRead = Math.min(5, readable);
        int i = k & 0x7F;
        for (int j = 1; j < maxRead; j++) {
            k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }
        throw new IllegalArgumentException("VarInt too long (length must be <= 5)");
    }

    // Based off of Andrew Steinborn's blog post:
    // https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
    @Override
    public void writeVarLong(ByteBuf buf, long value) {
        // Peel the one and two byte count cases explicitly as they are the most common VarInt sizes
        // that the server will write, to improve inlining.
        if ((value & ~0x7FL) == 0) {
            buf.writeByte((byte) value);
        } else if ((value & ~0x3FFFL) == 0) {
            int w = (int) ((value & 0x7FL | 0x80L) << 8 |
                    (value >>> 7));
            buf.writeShort(w);
        } else {
            writeVarLongFull(buf, value);
        }
    }

    private static void writeVarLongFull(ByteBuf buf, long value) {
        if ((value & ~0x7FL) == 0) {
            buf.writeByte((byte) value);
        } else if ((value & ~0x3FFFL) == 0) {
            int w = (int) ((value & 0x7FL | 0x80L) << 8 |
                    (value >>> 7));
            buf.writeShort(w);
        } else if ((value & ~0x1FFFFFL) == 0) {
            int w = (int) ((value & 0x7FL | 0x80L) << 16 |
                    ((value >>> 7) & 0x7FL | 0x80L) << 8 |
                    (value >>> 14));
            buf.writeMedium(w);
        } else if ((value & ~0xFFFFFFFL) == 0) {
            int w = (int) ((value & 0x7F | 0x80) << 24 |
                    (((value >>> 7) & 0x7F | 0x80) << 16) |
                    ((value >>> 14) & 0x7F | 0x80) << 8 |
                    (value >>> 21));
            buf.writeInt(w);
        } else if ((value & ~0x7FFFFFFFFL) == 0) {
            int w = (int) ((value & 0x7F | 0x80) << 24 |
                    ((value >>> 7) & 0x7F | 0x80) << 16 |
                    ((value >>> 14) & 0x7F | 0x80) << 8 |
                    ((value >>> 21) & 0x7F | 0x80));
            buf.writeInt(w);
            buf.writeByte((int) (value >>> 28));
        } else if ((value & ~0x3FFFFFFFFFFL) == 0) {
            int w = (int) ((value & 0x7F | 0x80) << 24 |
                    ((value >>> 7) & 0x7F | 0x80) << 16 |
                    ((value >>> 14) & 0x7F | 0x80) << 8 |
                    ((value >>> 21) & 0x7F | 0x80));
            int w2 = (int) (((value >>> 28) & 0x7FL | 0x80L) << 8 |
                    (value >>> 35));
            buf.writeInt(w);
            buf.writeShort(w2);
        } else if ((value & ~0x1FFFFFFFFFFFFL) == 0) {
            int w = (int) ((value & 0x7F | 0x80) << 24 |
                    ((value >>> 7) & 0x7F | 0x80) << 16 |
                    ((value >>> 14) & 0x7F | 0x80) << 8 |
                    ((value >>> 21) & 0x7F | 0x80));
            int w2 = (int) ((((value >>> 28) & 0x7FL | 0x80L) << 16 |
                    ((value >>> 35) & 0x7FL | 0x80L) << 8) |
                    (value >>> 42));
            buf.writeInt(w);
            buf.writeMedium(w2);
        } else if ((value & ~0xFFFFFFFFFFFFFFL) == 0) {
            long w = (value & 0x7F | 0x80) << 56 |
                    ((value >>> 7) & 0x7F | 0x80) << 48 |
                    ((value >>> 14) & 0x7F | 0x80) << 40 |
                    ((value >>> 21) & 0x7F | 0x80) << 32 |
                    ((value >>> 28) & 0x7FL | 0x80L) << 24 |
                    ((value >>> 35) & 0x7FL | 0x80L) << 16 |
                    ((value >>> 42) & 0x7FL | 0x80L) << 8 |
                    (value >>> 49);
            buf.writeLong(w);
        } else if ((value & ~0x7FFFFFFFFFFFFFFFL) == 0) {
            long w = (value & 0x7F | 0x80) << 56 |
                    ((value >>> 7) & 0x7F | 0x80) << 48 |
                    ((value >>> 14) & 0x7F | 0x80) << 40 |
                    ((value >>> 21) & 0x7F | 0x80) << 32 |
                    ((value >>> 28) & 0x7FL | 0x80L) << 24 |
                    ((value >>> 35) & 0x7FL | 0x80L) << 16 |
                    ((value >>> 42) & 0x7FL | 0x80L) << 8 |
                    (value >>> 49);
            buf.writeLong(w);
            buf.writeByte((byte) (value >>> 56));
        } else {
            long w = (value & 0x7F | 0x80) << 56 |
                    ((value >>> 7) & 0x7F | 0x80) << 48 |
                    ((value >>> 14) & 0x7F | 0x80) << 40 |
                    ((value >>> 21) & 0x7F | 0x80) << 32 |
                    ((value >>> 28) & 0x7FL | 0x80L) << 24 |
                    ((value >>> 35) & 0x7FL | 0x80L) << 16 |
                    ((value >>> 42) & 0x7FL | 0x80L) << 8 |
                    (value >>> 49);
            int w2 = (int) (((value >>> 56) & 0x7FL | 0x80L) << 8 |
                    (value >>> 63));
            buf.writeLong(w);
            buf.writeShort(w2);
        }
    }

    @Override
    public long readVarLong(ByteBuf buf) {
        int value = 0;
        int size = 0;
        int b;
        while (((b = buf.readByte()) & 0x80) == 0x80) {
            value |= (b & 0x7F) << (size++ * 7);
            if (size > 10) {
                throw new IllegalArgumentException("VarLong too long (length must be <= 10)");
            }
        }

        return value | ((b & 0x7FL) << (size * 7));
    }

    @Override
    public String readString(ByteBuf buf) {
        return this.readString(buf, 262143);
    }

    @Override
    public String readString(ByteBuf buf, int maxLength) {
        int length = this.readVarInt(buf);
        if (length > maxLength * 3) {
            throw new IllegalArgumentException("String buffer is longer than maximum allowed length");
        }
        String string = (String) buf.readCharSequence(length, StandardCharsets.UTF_8);
        if (string.length() > maxLength) {
            throw new IllegalArgumentException("String is longer than maximum allowed length");
        }

        return string;
    }

    @Override
    public void writeString(ByteBuf buf, String value) {
        this.writeVarInt(buf, ByteBufUtil.utf8Bytes(value));
        buf.writeCharSequence(value, StandardCharsets.UTF_8);
    }
}
