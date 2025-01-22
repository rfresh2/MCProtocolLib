package org.geysermc.mcprotocollib.protocol.codec;

import com.google.gson.JsonElement;
import com.viaversion.nbt.io.MNBTIO;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.mini.MNBT;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.math.vector.Vector4f;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.DefaultComponentSerializer;
import org.geysermc.mcprotocollib.protocol.data.game.Holder;
import org.geysermc.mcprotocollib.protocol.data.game.Identifier;
import org.geysermc.mcprotocollib.protocol.data.game.chat.ChatType;
import org.geysermc.mcprotocollib.protocol.data.game.chat.ChatTypeDecoration;
import org.geysermc.mcprotocollib.protocol.data.game.chat.numbers.BlankFormat;
import org.geysermc.mcprotocollib.protocol.data.game.chat.numbers.FixedFormat;
import org.geysermc.mcprotocollib.protocol.data.game.chat.numbers.NumberFormat;
import org.geysermc.mcprotocollib.protocol.data.game.chat.numbers.StyledFormat;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.ListPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.MapPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.Palette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.SingletonPalette;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EntityEvent;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.ModifierOperation;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.ArmadilloState;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.GlobalPos;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.PaintingVariant;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.Pose;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.SnifferState;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.VillagerData;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.WolfVariant;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.BlockBreakStage;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponent;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
import org.geysermc.mcprotocollib.protocol.data.game.level.event.LevelEvent;
import org.geysermc.mcprotocollib.protocol.data.game.level.event.LevelEventType;
import org.geysermc.mcprotocollib.protocol.data.game.level.event.UnknownLevelEvent;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.BlockParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.DustColorTransitionParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.DustParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.EntityEffectParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.ItemParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.Particle;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.ParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.ParticleType;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.SculkChargeParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.ShriekParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.VibrationParticleData;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.positionsource.BlockPositionSource;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.positionsource.EntityPositionSource;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.positionsource.PositionSource;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.positionsource.PositionSourceType;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.CustomSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.SoundCategory;
import org.geysermc.mcprotocollib.protocol.data.game.recipe.Ingredient;
import org.geysermc.mcprotocollib.protocol.data.game.statistic.StatisticCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MinecraftTypes {
    private static final int POSITION_X_SIZE = 38;
    private static final int POSITION_Y_SIZE = 12;
    private static final int POSITION_Z_SIZE = 38;
    private static final int POSITION_Y_SHIFT = 0xFFF;
    private static final int POSITION_WRITE_SHIFT = 0x3FFFFFF;
    public static boolean useBinaryNbtComponentSerializer = true;

    public static void writeVarInt(ByteBuf buf, int value) {
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

    public static void write21BitVarInt(ByteBuf buf, int value) {
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

    public static int readVarInt(ByteBuf buf) {
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

    public static void writeVarLong(ByteBuf buf, long value) {
        while ((value & -128L) != 0) {
            buf.writeByte((int) (value & 127L) | 128);
            value >>>= 7;
        }

        buf.writeByte((int) value);
    }

    public static long readVarLong(ByteBuf buf) {
        long value = 0L;
        int i = 0;

        byte b;
        do {
            if (i >= 10) {
                throw new RuntimeException("VarLong wider than 10 bytes");
            }
            b = buf.readByte();
            value |= (long) (b & 127) << i++ * 7;
        } while ((b & 128) == 128);

        return value;
    }

    public static String readString(ByteBuf buf) {
        return readString(buf, 262144);
    }

    public static String readString(ByteBuf buf, int maxLength) {
        int length = readVarInt(buf);
        if (length > maxLength * 3) {
            throw new IllegalArgumentException("String buffer is longer than maximum allowed length");
        }
        String string = (String) buf.readCharSequence(length, StandardCharsets.UTF_8);
        if (string.length() > maxLength) {
            throw new IllegalArgumentException("String is longer than maximum allowed length");
        }

        return string;
    }

    public static void writeString(ByteBuf buf, String value) {
        writeVarInt(buf, ByteBufUtil.utf8Bytes(value));
        buf.writeCharSequence(value, StandardCharsets.UTF_8);
    }

    @Nullable
    public static <T> T readNullable(ByteBuf buf, Function<ByteBuf, T> ifPresent) {
        if (buf.readBoolean()) {
            return ifPresent.apply(buf);
        } else {
            return null;
        }
    }

    public static <T> void writeNullable(ByteBuf buf, @Nullable T value, BiConsumer<ByteBuf, T> ifPresent) {
        if (value != null) {
            buf.writeBoolean(true);
            ifPresent.accept(buf, value);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static <T> List<T> readList(ByteBuf buf, Function<ByteBuf, T> reader) {
        int size = readVarInt(buf);
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(reader.apply(buf));
        }

        return list;
    }

    public static <T> void writeList(ByteBuf buf, List<T> value, BiConsumer<ByteBuf, T> writer) {
        writeVarInt(buf, value.size());
        for (int i = 0; i < value.size(); i++) {
            writer.accept(buf, value.get(i));
        }
    }

    public static <T> Holder<T> readHolder(ByteBuf buf, Function<ByteBuf, T> readCustom) {
        int registryId = readVarInt(buf);
        return registryId == 0 ? Holder.ofCustom(readCustom.apply(buf)) : Holder.ofId(registryId - 1);
    }

    public static <T> void writeHolder(ByteBuf buf, Holder<T> holder, BiConsumer<ByteBuf, T> writeCustom) {
        if (holder.isCustom()) {
            writeVarInt(buf, 0);
            writeCustom.accept(buf, holder.custom());
        } else {
            writeVarInt(buf, holder.id() + 1);
        }
    }

    @SuppressWarnings("PatternValidation")
    public static Key readResourceLocation(ByteBuf buf) {
        return Key.key(readString(buf));
    }

    public static String readResourceLocationString(ByteBuf buf) {
        return Identifier.formalize(readString(buf));
    }

    public static void writeResourceLocation(ByteBuf buf, Key location) {
        writeString(buf, location.asString());
    }

    public static void writeResourceLocation(ByteBuf buf, String location) {
        writeString(buf, location);
    }

    public static UUID readUUID(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUUID(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static byte[] readByteArray(ByteBuf buf) {
        return readByteArray(buf, MinecraftTypes::readVarInt);
    }

    public static byte[] readByteArray(ByteBuf buf, ToIntFunction<ByteBuf> reader) {
        int length = reader.applyAsInt(buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return bytes;
    }

    public static void writeByteArray(ByteBuf buf, byte[] bytes) {
        writeByteArray(buf, bytes, MinecraftTypes::writeVarInt);
    }

    public static void writeByteArray(ByteBuf buf, byte[] bytes, ObjIntConsumer<ByteBuf> writer) {
        writer.accept(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static long[] readLongArray(ByteBuf buf) {
        return readLongArray(buf, MinecraftTypes::readVarInt);
    }

    public static long[] readLongArray(ByteBuf buf, ToIntFunction<ByteBuf> reader) {
        int length = reader.applyAsInt(buf);
        if (length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }

        long[] l = new long[length];
        for (int index = 0; index < length; index++) {
            l[index] = buf.readLong();
        }

        return l;
    }

    public static void writeLongArray(ByteBuf buf, long[] l) {
        writeLongArray(buf, l, MinecraftTypes::writeVarInt);
    }

    public static void writeLongArray(ByteBuf buf, long[] l, ObjIntConsumer<ByteBuf> writer) {
        writer.accept(buf, l.length);
        for (long value : l) {
            buf.writeLong(value);
        }
    }

    @Nullable
    public static CompoundTag readTag(ByteBuf buf) {
        return readTag(buf, CompoundTag.class);
    }

    @NonNull
    public static CompoundTag readTagOrThrow(ByteBuf buf) {
        CompoundTag tag = readTag(buf);
        if (tag == null) {
            throw new IllegalArgumentException("Got end-tag when trying to read CompoundTag");
        }
        return tag;
    }

    @Nullable
    public static <T extends Tag> T readTag(ByteBuf buf, Class<T> expected) {
        if (buf.readByte() == 0) {
            return null;
        }
        buf.readerIndex(buf.readerIndex() - 1);
        try (DataInputStream in = byteBufToDataInputStream(buf)) {
            return NBTIO.readTag(in, TagLimiter.noop(), false, expected);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T extends Tag> void writeTag(ByteBuf buf, @Nullable T tag) throws UncheckedIOException {
        if (tag == null) {
            buf.writeByte(0);
            return;
        }
        try (DataOutputStream out = byteBufToDataOutputStream(buf)) {
            NBTIO.writeTag(out, tag, false);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T extends Tag> void writeNamedTag(ByteBuf buf, CompoundTag tag, String name) throws UncheckedIOException {
        try (DataOutputStream out = byteBufToDataOutputStream(buf)) {
            NBTIO.writeTag(out, tag, false);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static MNBT readNamedMNBT(ByteBuf buf) throws UncheckedIOException {
        try (DataInputStream in = byteBufToDataInputStream(buf)) {
            var mnbt = MNBTIO.read(in, true);
            if (mnbt.isEmpty()) return null;
            else return mnbt;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static MNBT readMNBT(ByteBuf buf) throws UncheckedIOException {
        try (DataInputStream in = byteBufToDataInputStream(buf)) {
            var mnbt = MNBTIO.read(in, false);
            if (mnbt.isEmpty()) return null;
            else return mnbt;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeMNBT(ByteBuf buf, MNBT mnbt) throws UncheckedIOException {
        try (DataOutputStream out = byteBufToDataOutputStream(buf)) {
            MNBTIO.write(out, mnbt);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DataInputStream byteBufToDataInputStream(ByteBuf buf) {
        return new DataInputStream(new InputStream() {
            @Override
            public int read() {
                return buf.readUnsignedByte();
            }
        });
    }

    private static DataOutputStream byteBufToDataOutputStream(ByteBuf buf) {
        return new DataOutputStream(new OutputStream() {
            @Override
            public void write(int b) {
                buf.writeByte(b);
            }
        });
    }

    @Nullable
    public static ItemStack readOptionalItemStack(ByteBuf buf) {
        byte count = buf.readByte();
        if (count <= 0) {
            return null;
        }

        int item = readVarInt(buf);
        return new ItemStack(item, count, readDataComponentPatch(buf));
    }

    public static void writeOptionalItemStack(ByteBuf buf, ItemStack item) {
        boolean empty = item == null || item.getAmount() <= 0;
        buf.writeByte(!empty ? item.getAmount() : 0);
        if (!empty) {
            writeVarInt(buf, item.getId());
            writeDataComponentPatch(buf, item.getDataComponents());
        }
    }

    @NotNull
    public static ItemStack readItemStack(ByteBuf buf) {
        return readOptionalItemStack(buf);
    }

    public static void writeItemStack(ByteBuf buf, @NotNull ItemStack item) {
        writeOptionalItemStack(buf, item);
    }

    @Nullable
    public static DataComponents readDataComponentPatch(ByteBuf buf) {
        int nonNullComponents = readVarInt(buf);
        int nullComponents = readVarInt(buf);
        if (nonNullComponents == 0 && nullComponents == 0) {
            return null;
        }

        Map<DataComponentType<?>, DataComponent<?, ?>> dataComponents = new HashMap<>(nonNullComponents + nullComponents);
        for (int k = 0; k < nonNullComponents; k++) {
            DataComponentType<?> dataComponentType = DataComponentType.from(readVarInt(buf));
            DataComponent<?, ?> dataComponent = dataComponentType.readDataComponent(buf);
            dataComponents.put(dataComponentType, dataComponent);
        }

        for (int k = 0; k < nullComponents; k++) {
            DataComponentType<?> dataComponentType = DataComponentType.from(readVarInt(buf));
            DataComponent<?, ?> dataComponent = dataComponentType.readNullDataComponent();
            dataComponents.put(dataComponentType, dataComponent);
        }

        return new DataComponents(dataComponents);
    }

    public static void writeDataComponentPatch(ByteBuf buf, DataComponents dataComponents) {
        if (dataComponents == null) {
            writeVarInt(buf, 0);
            writeVarInt(buf, 0);
        } else {
            int i = 0;
            int j = 0;
            for (DataComponent<?, ?> component : dataComponents.getDataComponents().values()) {
                if (component.getValue() != null) {
                    i++;
                } else {
                    j++;
                }
            }

            writeVarInt(buf, i);
            writeVarInt(buf, j);

            for (DataComponent<?, ?> component : dataComponents.getDataComponents().values()) {
                if (component.getValue() != null) {
                    writeVarInt(buf, component.getType().getId());
                    component.write(buf);
                }
            }

            for (DataComponent<?, ?> component : dataComponents.getDataComponents().values()) {
                if (component.getValue() == null) {
                    writeVarInt(buf, component.getType().getId());
                }
            }
        }
    }

    @NotNull
    public static ItemStack readTradeItemStack(ByteBuf buf) {
        int item = readVarInt(buf);
        int count = readVarInt(buf);
        int componentsLength = readVarInt(buf);

        Map<DataComponentType<?>, DataComponent<?, ?>> dataComponents = new HashMap<>(componentsLength);
        for (int i = 0; i < componentsLength; i++) {
            DataComponentType<?> dataComponentType = DataComponentType.from(readVarInt(buf));
            DataComponent<?, ?> dataComponent = dataComponentType.readDataComponent(buf);
            dataComponents.put(dataComponentType, dataComponent);
        }

        return new ItemStack(item, count, new DataComponents(dataComponents));
    }

    public static void writeTradeItemStack(ByteBuf buf, @NotNull ItemStack item) {
        writeVarInt(buf, item.getId());
        writeVarInt(buf, item.getAmount());

        DataComponents dataComponents = item.getDataComponents();
        if (dataComponents == null) {
            writeVarInt(buf, 0);
            return;
        }

        writeVarInt(buf, dataComponents.getDataComponents().size());
        for (DataComponent<?, ?> component : dataComponents.getDataComponents().values()) {
            writeVarInt(buf, component.getType().getId());
            component.write(buf);
        }
    }

    public static Vector3i readPosition(ByteBuf buf) {
        long val = buf.readLong();

        int x = (int) (val >> POSITION_X_SIZE);
        int y = (int) (val << 52 >> 52);
        int z = (int) (val << 26 >> POSITION_Z_SIZE);

        return Vector3i.from(x, y, z);
    }

    public static int decodePositionX(long position) {
        return (int) (position >> POSITION_X_SIZE);
    }

    public static int decodePositionY(long position) {
        return (int) (position << 52 >> 52);
    }

    public static int decodePositionZ(long position) {
        return (int) (position << 26 >> POSITION_Z_SIZE);
    }

    public static void writePosition(ByteBuf buf, Vector3i pos) {
        long x = pos.getX() & POSITION_WRITE_SHIFT;
        long y = pos.getY() & POSITION_Y_SHIFT;
        long z = pos.getZ() & POSITION_WRITE_SHIFT;

        buf.writeLong(x << POSITION_X_SIZE | z << POSITION_Y_SIZE | y);
    }

    public static void writePosition(ByteBuf buf, int posX, int posY, int posZ) {
        long x = posX & POSITION_WRITE_SHIFT;
        long y = posY & POSITION_Y_SHIFT;
        long z = posZ & POSITION_WRITE_SHIFT;

        buf.writeLong(x << POSITION_X_SIZE | z << POSITION_Y_SIZE | y);
    }

    public static Vector3f readRotation(ByteBuf buf) {
        float x = buf.readFloat();
        float y = buf.readFloat();
        float z = buf.readFloat();

        return Vector3f.from(x, y, z);
    }

    public static void writeRotation(ByteBuf buf, Vector3f rot) {
        buf.writeFloat(rot.getX());
        buf.writeFloat(rot.getY());
        buf.writeFloat(rot.getZ());
    }

    public static Vector4f readQuaternion(ByteBuf buf) {
        float x = buf.readFloat();
        float y = buf.readFloat();
        float z = buf.readFloat();
        float w = buf.readFloat();

        return Vector4f.from(x, y, z, w);
    }

    public static void writeQuaternion(ByteBuf buf, Vector4f vec4) {
        buf.writeFloat(vec4.getX());
        buf.writeFloat(vec4.getY());
        buf.writeFloat(vec4.getZ());
        buf.writeFloat(vec4.getW());
    }

    public static Direction readDirection(ByteBuf buf) {
        return Direction.from(readVarInt(buf));
    }

    public static void writeDirection(ByteBuf buf, Direction dir) {
        writeEnum(buf, dir);
    }

    public static Pose readPose(ByteBuf buf) {
        return Pose.from(readVarInt(buf));
    }

    public static void writePose(ByteBuf buf, Pose pose) {
        writeEnum(buf, pose);
    }

    public static Holder<WolfVariant> readWolfVariant(ByteBuf buf) {
        return readHolder(buf, input -> {
            String wildTexture = readResourceLocationString(input);
            String tameTexture = readResourceLocationString(input);
            String angryTexture = readResourceLocationString(input);
            String biomeLocation = null;
            int[] biomeHolders = null;

            int length = readVarInt(input) - 1;
            if (length == -1) {
                biomeLocation = readResourceLocationString(input);
            } else {
                biomeHolders = new int[length];
                for (int j = 0; j < length; j++) {
                    biomeHolders[j] = readVarInt(input);
                }
            }
            return new WolfVariant(wildTexture, tameTexture, angryTexture, biomeLocation, biomeHolders);
        });
    }

    public static void writeWolfVariant(ByteBuf buf, Holder<WolfVariant> variantHolder) {
        writeHolder(buf, variantHolder, (output, variant) -> {
            writeResourceLocation(output, variant.wildTexture());
            writeResourceLocation(output, variant.tameTexture());
            writeResourceLocation(output, variant.angryTexture());
            if (variant.biomeLocation() != null) {
                writeVarInt(output, 0);
                writeResourceLocation(output, variant.biomeLocation());
            } else {
                writeVarInt(output, variant.biomeHolders().length + 1);
                for (int holder : variant.biomeHolders()) {
                    writeVarInt(output, holder);
                }
            }
        });
    }

    public static Holder<PaintingVariant> readPaintingVariant(ByteBuf buf) {
        return readHolder(buf, input -> {
            return new PaintingVariant(readVarInt(input), readVarInt(input), readResourceLocationString(input));
        });
    }

    public static void writePaintingVariant(ByteBuf buf, Holder<PaintingVariant> variantHolder) {
        writeHolder(buf, variantHolder, (output, variant) -> {
            writeVarInt(buf, variant.width());
            writeVarInt(buf, variant.height());
            writeResourceLocation(buf, variant.assetId());
        });
    }

    public static SnifferState readSnifferState(ByteBuf buf) {
        return SnifferState.from(readVarInt(buf));
    }

    public static void writeSnifferState(ByteBuf buf, SnifferState state) {
        writeEnum(buf, state);
    }

    public static ArmadilloState readArmadilloState(ByteBuf buf) {
        return ArmadilloState.from(readVarInt(buf));
    }

    public static void writeArmadilloState(ByteBuf buf, ArmadilloState state) {
        writeEnum(buf, state);
    }

    private static void writeEnum(ByteBuf buf, Enum<?> e) {
        writeVarInt(buf, e.ordinal());
    }

    public static Component readComponent(ByteBuf buf) {
        // do not use CompoundTag, as mojang serializes a plaintext component as just a single StringTag
        Tag tag = readTag(buf, null);
        if (tag == null) {
            throw new IllegalArgumentException("Got end-tag when trying to read Component");
        }
        JsonElement json = NbtComponentSerializer.tagComponentToJson(tag);
        return DefaultComponentSerializer.get().deserializeFromTree(json);
    }

    public static void writeComponent(ByteBuf buf, Component component) {
        if (useBinaryNbtComponentSerializer) {
            try (DataOutputStream out = byteBufToDataOutputStream(buf)) {
                BinaryNbtComponentSerializer.serializeMNBTToBuffer(component, out);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            var json = DefaultComponentSerializer.get().serializeToTree(component);
            var tag = NbtComponentSerializer.jsonComponentToTag(json);
            writeTag(buf, tag);
        }
    }

    public static List<EntityMetadata<?, ?>> readEntityMetadata(ByteBuf buf) {
        List<EntityMetadata<?, ?>> ret = new ArrayList<>();
        int id;
        while ((id = buf.readUnsignedByte()) != 255) {
            ret.add(readMetadata(buf, id));
        }

        return ret;
    }

    public static void writeEntityMetadata(ByteBuf buf, List<EntityMetadata<?, ?>> metadata) {
        for (int i = 0; i < metadata.size(); i++) {
            writeMetadata(buf, metadata.get(i));
        }

        buf.writeByte(255);
    }

    public static EntityMetadata<?, ?> readMetadata(ByteBuf buf, int id) {
        MetadataType<?> type = readMetadataType(buf);
        return type.readMetadata(buf, id);
    }

    public static void writeMetadata(ByteBuf buf, EntityMetadata<?, ?> metadata) {
        buf.writeByte(metadata.getId());
        writeMetadataType(buf, metadata.getType());
        metadata.write(buf);
    }

    public static MetadataType<?> readMetadataType(ByteBuf buf) {
        int id = readVarInt(buf);
        if (id >= MetadataType.size()) {
            throw new IllegalArgumentException("Received id " + id + " for MetadataType when the maximum was " + MetadataType.size() + "!");
        }

        return MetadataType.from(id);
    }

    public static void writeMetadataType(ByteBuf buf, MetadataType<?> type) {
        writeVarInt(buf, type.getId());
    }

    public static GlobalPos readGlobalPos(ByteBuf buf) {
        Key dimension = readResourceLocation(buf);
        var position = buf.readLong();
        int x = decodePositionX(position);
        int y = decodePositionY(position);
        int z = decodePositionZ(position);
        return new GlobalPos(dimension, x, y, z);
    }

    public static void writeGlobalPos(ByteBuf buf, GlobalPos pos) {
        writeResourceLocation(buf, pos.getDimension());
        writePosition(buf, pos.getX(), pos.getY(), pos.getZ());
    }

    public static PlayerSpawnInfo readPlayerSpawnInfo(ByteBuf buf) {
        int dimension = readVarInt(buf);
        Key worldName = readResourceLocation(buf);
        long hashedSeed = buf.readLong();
        GameMode gameMode = GameMode.byId(buf.readByte());
        GameMode previousGamemode = GameMode.byNullableId(buf.readByte());
        boolean debug = buf.readBoolean();
        boolean flat = buf.readBoolean();
        GlobalPos lastDeathPos = readNullable(buf, MinecraftTypes::readGlobalPos);
        int portalCooldown = readVarInt(buf);
        return new PlayerSpawnInfo(dimension, worldName, hashedSeed, gameMode, previousGamemode, debug, flat, lastDeathPos, portalCooldown);
    }

    public static void writePlayerSpawnInfo(ByteBuf buf, PlayerSpawnInfo info) {
        writeVarInt(buf, info.getDimension());
        writeResourceLocation(buf, info.getWorldName());
        buf.writeLong(info.getHashedSeed());
        buf.writeByte(info.getGameMode().ordinal());
        buf.writeByte(GameMode.toNullableId(info.getPreviousGamemode()));
        buf.writeBoolean(info.isDebug());
        buf.writeBoolean(info.isFlat());
        writeNullable(buf, info.getLastDeathPos(), MinecraftTypes::writeGlobalPos);
        writeVarInt(buf, info.getPortalCooldown());
    }

    public static ParticleType readParticleType(ByteBuf buf) {
        return ParticleType.from(readVarInt(buf));
    }

    public static void writeParticleType(ByteBuf buf, ParticleType type) {
        writeEnum(buf, type);
    }

    public static Particle readParticle(ByteBuf buf) {
        ParticleType particleType = readParticleType(buf);
        return new Particle(particleType, readParticleData(buf, particleType));
    }

    public static void writeParticle(ByteBuf buf, Particle particle) {
        writeEnum(buf, particle.getType());
        writeParticleData(buf, particle.getType(), particle.getData());
    }

    public static ParticleData readParticleData(ByteBuf buf, ParticleType type) {
        return switch (type) {
            case BLOCK, BLOCK_MARKER, FALLING_DUST, DUST_PILLAR -> new BlockParticleData(readVarInt(buf));
            case DUST -> {
                float red = buf.readFloat();
                float green = buf.readFloat();
                float blue = buf.readFloat();
                float scale = buf.readFloat();
                yield new DustParticleData(red, green, blue, scale);
            }
            case DUST_COLOR_TRANSITION -> {
                float red = buf.readFloat();
                float green = buf.readFloat();
                float blue = buf.readFloat();
                float newRed = buf.readFloat();
                float newGreen = buf.readFloat();
                float newBlue = buf.readFloat();
                float scale = buf.readFloat();
                yield new DustColorTransitionParticleData(red, green, blue, scale, newRed, newGreen, newBlue);
            }
            case ENTITY_EFFECT -> new EntityEffectParticleData(buf.readInt());
            case ITEM -> new ItemParticleData(readOptionalItemStack(buf));
            case SCULK_CHARGE -> new SculkChargeParticleData(buf.readFloat());
            case SHRIEK -> new ShriekParticleData(readVarInt(buf));
            case VIBRATION -> new VibrationParticleData(readPositionSource(buf), readVarInt(buf));
            default -> null;
        };
    }

    public static void writeParticleData(ByteBuf buf, ParticleType type, ParticleData data) {
        switch (type) {
            case BLOCK, BLOCK_MARKER, FALLING_DUST, DUST_PILLAR -> {
                BlockParticleData blockData = (BlockParticleData) data;
                writeVarInt(buf, blockData.getBlockState());
            }
            case DUST -> {
                DustParticleData dustData = (DustParticleData) data;
                buf.writeFloat(dustData.getRed());
                buf.writeFloat(dustData.getGreen());
                buf.writeFloat(dustData.getBlue());
                buf.writeFloat(dustData.getScale());
            }
            case DUST_COLOR_TRANSITION -> {
                DustColorTransitionParticleData dustData = (DustColorTransitionParticleData) data;
                buf.writeFloat(dustData.getRed());
                buf.writeFloat(dustData.getGreen());
                buf.writeFloat(dustData.getBlue());
                buf.writeFloat(dustData.getNewRed());
                buf.writeFloat(dustData.getNewGreen());
                buf.writeFloat(dustData.getNewBlue());
                buf.writeFloat(dustData.getScale());
            }
            case ENTITY_EFFECT -> {
                EntityEffectParticleData entityEffectData = (EntityEffectParticleData) data;
                buf.writeInt(entityEffectData.getColor());
            }
            case ITEM -> {
                ItemParticleData itemData = (ItemParticleData) data;
                writeOptionalItemStack(buf, itemData.getItemStack());
            }
            case SCULK_CHARGE -> {
                SculkChargeParticleData sculkData = (SculkChargeParticleData) data;
                buf.writeFloat(sculkData.getRoll());
            }
            case SHRIEK -> {
                ShriekParticleData shriekData = (ShriekParticleData) data;
                writeVarInt(buf, shriekData.getDelay());
            }
            case VIBRATION -> {
                VibrationParticleData vibrationData = (VibrationParticleData) data;
                writePositionSource(buf, vibrationData.getPositionSource());
                writeVarInt(buf, vibrationData.getArrivalTicks());
            }
        }
    }

    public static NumberFormat readNumberFormat(ByteBuf buf) {
        int id = readVarInt(buf);
        return switch (id) {
            case 0 -> BlankFormat.INSTANCE;
            case 1 -> new StyledFormat(readMNBT(buf));
            case 2 -> new FixedFormat(readComponent(buf));
            default -> throw new IllegalArgumentException("Unknown number format type: " + id);
        };
    }

    public static void writeNumberFormat(ByteBuf buf, NumberFormat numberFormat) {
        if (numberFormat instanceof BlankFormat) {
            writeVarInt(buf, 0);
        } else if (numberFormat instanceof StyledFormat styledFormat) {
            writeVarInt(buf, 1);
            writeMNBT(buf, styledFormat.getStyle());
        } else if (numberFormat instanceof FixedFormat fixedFormat) {
            writeVarInt(buf, 2);
            writeComponent(buf, fixedFormat.getValue());
        } else {
            throw new IllegalArgumentException("Unknown number format: " + numberFormat);
        }
    }

    public static ChatType readChatType(ByteBuf buf) {
        return new ChatType(readChatTypeDecoration(buf), readChatTypeDecoration(buf));
    }

    public static void writeChatType(ByteBuf buf, ChatType chatType) {
        writeChatTypeDecoration(buf, chatType.chat());
        writeChatTypeDecoration(buf, chatType.narration());
    }

    public static ChatTypeDecoration readChatTypeDecoration(ByteBuf buf) {
        String translationKey = readString(buf);

        int size = readVarInt(buf);
        List<ChatTypeDecoration.Parameter> parameters = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            parameters.add(ChatTypeDecoration.Parameter.VALUES[readVarInt(buf)]);
        }

        MNBT style = readMNBT(buf);
        return new ChatType.ChatTypeDecorationImpl(translationKey, parameters, style);
    }

    public static void writeChatTypeDecoration(ByteBuf buf, ChatTypeDecoration decoration) {
        writeString(buf, decoration.translationKey());

        writeVarInt(buf, decoration.parameters().size());
        List<ChatTypeDecoration.Parameter> parameters = decoration.parameters();
        for (int i = 0; i < parameters.size(); i++) {
            ChatTypeDecoration.Parameter parameter = parameters.get(i);
            writeVarInt(buf, parameter.ordinal());
        }

        writeMNBT(buf, decoration.style());
    }

    public static PositionSource readPositionSource(ByteBuf buf) {
        PositionSourceType type = PositionSourceType.from(readVarInt(buf));
        return switch (type) {
            case BLOCK -> new BlockPositionSource(readPosition(buf));
            case ENTITY -> new EntityPositionSource(readVarInt(buf), buf.readFloat());
        };
    }

    public static void writePositionSource(ByteBuf buf, PositionSource positionSource) {
        writeVarInt(buf, positionSource.getType().ordinal());
        if (positionSource instanceof BlockPositionSource blockPositionSource) {
            writePosition(buf, blockPositionSource.getPosition());
        } else if (positionSource instanceof EntityPositionSource entityPositionSource) {
            writeVarInt(buf, entityPositionSource.getEntityId());
            buf.writeFloat(entityPositionSource.getYOffset());
        } else {
            throw new IllegalStateException("Unknown position source type!");
        }
    }

    public static VillagerData readVillagerData(ByteBuf buf) {
        return new VillagerData(readVarInt(buf), readVarInt(buf), readVarInt(buf));
    }

    public static void writeVillagerData(ByteBuf buf, VillagerData villagerData) {
        writeVarInt(buf, villagerData.getType());
        writeVarInt(buf, villagerData.getProfession());
        writeVarInt(buf, villagerData.getLevel());
    }

    public static ModifierOperation readModifierOperation(ByteBuf buf) {
        return ModifierOperation.from(buf.readByte());
    }

    public static void writeModifierOperation(ByteBuf buf, ModifierOperation operation) {
        buf.writeByte(operation.ordinal());
    }

    public static Effect readEffect(ByteBuf buf) {
        return Effect.from(readVarInt(buf));
    }

    public static void writeEffect(ByteBuf buf, Effect effect) {
        writeVarInt(buf, effect.ordinal());
    }

    public static BlockBreakStage readBlockBreakStage(ByteBuf buf) {
        int stage = buf.readUnsignedByte();
        if (stage >= 0 && stage < 10) {
            return BlockBreakStage.STAGES[stage];
        } else {
            return BlockBreakStage.RESET;
        }
    }

    public static void writeBlockBreakStage(ByteBuf buf, BlockBreakStage stage) {
        if (stage == BlockBreakStage.RESET) {
            buf.writeByte(255);
        } else {
            buf.writeByte(stage.ordinal());
        }
    }

    @Nullable
    public static BlockEntityType readBlockEntityType(ByteBuf buf) {
        return BlockEntityType.from(readVarInt(buf));
    }

    public static void writeBlockEntityType(ByteBuf buf, BlockEntityType type) {
        writeEnum(buf, type);
    }

    public static LightUpdateData readLightUpdateData(ByteBuf buf) {
        var skyYMask = readLongArray(buf);
        var blockYMask = readLongArray(buf);
        var emptySkyYMask = readLongArray(buf);
        var emptyBlockYMask = readLongArray(buf);

        int skyUpdateSize = readVarInt(buf);
        var skyUpdates = new ArrayList<byte[]>(skyUpdateSize);
        for (int i = 0; i < skyUpdateSize; i++) {
            skyUpdates.add(readByteArray(buf));
        }

        int blockUpdateSize = readVarInt(buf);
        var blockUpdates = new ArrayList<byte[]>(blockUpdateSize);
        for (int i = 0; i < blockUpdateSize; i++) {
            blockUpdates.add(readByteArray(buf));
        }
        return new LightUpdateData(skyYMask, blockYMask, emptySkyYMask, emptyBlockYMask, skyUpdates, blockUpdates);
    }

    public static void writeLightUpdateData(ByteBuf buf, LightUpdateData data) {
        writeLongArray(buf, data.getSkyYMask());
        writeLongArray(buf, data.getBlockYMask());
        writeLongArray(buf, data.getEmptySkyYMask());
        writeLongArray(buf, data.getEmptyBlockYMask());

        writeVarInt(buf, data.getSkyUpdates().size());
        for (byte[] array : data.getSkyUpdates()) {
            writeByteArray(buf, array);
        }

        writeVarInt(buf, data.getBlockUpdates().size());
        for (byte[] array : data.getBlockUpdates()) {
            writeByteArray(buf, array);
        }
    }

    private static void writeBitSet(ByteBuf buf, BitSet bitSet) {
        long[] array = bitSet.toLongArray();
        writeLongArray(buf, array);
    }

    public static LevelEvent readLevelEvent(ByteBuf buf) {
        int id = buf.readInt();
        LevelEventType type = LevelEventType.from(id);
        if (type != null) {
            return type;
        }
        return new UnknownLevelEvent(id);
    }

    public static void writeLevelEvent(ByteBuf buf, LevelEvent event) {
        buf.writeInt(event.getId());
    }

    public static StatisticCategory readStatisticCategory(ByteBuf buf) {
        return StatisticCategory.from(readVarInt(buf));
    }

    public static void writeStatisticCategory(ByteBuf buf, StatisticCategory category) {
        writeEnum(buf, category);
    }

    public static SoundCategory readSoundCategory(ByteBuf buf) {
        return SoundCategory.from(readVarInt(buf));
    }

    public static void writeSoundCategory(ByteBuf buf, SoundCategory category) {
        writeEnum(buf, category);
    }

    public static EntityEvent readEntityEvent(ByteBuf buf) {
        return EntityEvent.from(buf.readByte());
    }

    public static void writeEntityEvent(ByteBuf buf, EntityEvent event) {
        buf.writeByte(event.ordinal());
    }

    public static Ingredient readRecipeIngredient(ByteBuf buf) {
        ItemStack[] options = new ItemStack[readVarInt(buf)];
        for (int i = 0; i < options.length; i++) {
            options[i] = readOptionalItemStack(buf);
        }

        return new Ingredient(options);
    }

    public static void writeRecipeIngredient(ByteBuf buf, Ingredient ingredient) {
        writeVarInt(buf, ingredient.getOptions().length);
        for (ItemStack option : ingredient.getOptions()) {
            writeOptionalItemStack(buf, option);
        }
    }

    public static DataPalette readDataPalette(ByteBuf buf, PaletteType paletteType) {
        int bitsPerEntry = buf.readByte() & 0xFF;
        Palette palette = switch (paletteType) {
            case CHUNK -> readChunkPalette(buf, bitsPerEntry);
            case BIOME -> readBiomePalette(buf, bitsPerEntry);
        };
        long[] data = readLongArray(buf);
        BitStorage storage;
        if (palette instanceof SingletonPalette) {
            storage = null;
        } else {
            storage = new BitStorage(bitsPerEntry, paletteType.getStorageSize(), data);
        }

        return new DataPalette(palette, storage, paletteType);
    }

    public static void writeDataPalette(ByteBuf buf, DataPalette palette) {
        if (palette.getPalette() instanceof SingletonPalette) {
            buf.writeByte(0); // Bits per entry
            writeVarInt(buf, palette.getPalette().idToState(0));
            writeVarInt(buf, 0); // Data length
            return;
        }

        buf.writeByte(palette.getStorage().getBitsPerEntry());

        if (!(palette.getPalette() instanceof GlobalPalette)) {
            int paletteLength = palette.getPalette().size();
            writeVarInt(buf, paletteLength);
            for (int i = 0; i < paletteLength; i++) {
                writeVarInt(buf, palette.getPalette().idToState(i));
            }
        }

        long[] data = palette.getStorage().getData();
        writeLongArray(buf, data);
    }

    private static Palette readChunkPalette(ByteBuf buf, int bitsPerEntry) {
        return switch (bitsPerEntry) {
            case 0 -> new SingletonPalette(buf);
            case 1,2,3 -> new ListPalette(bitsPerEntry, buf);
            case 4,5,6,7,8 -> new MapPalette(bitsPerEntry, buf);
            default -> GlobalPalette.INSTANCE;
        };
    }

    private static Palette readBiomePalette(ByteBuf buf, int bitsPerEntry) {
        return switch (bitsPerEntry) {
            case 0 -> new SingletonPalette(buf);
            case 1,2,3 -> new ListPalette(bitsPerEntry, buf);
            default -> GlobalPalette.INSTANCE;
        };
    }

    public static ChunkSection readChunkSection(ByteBuf buf) {
        int blockCount = buf.readShort();

        DataPalette chunkPalette = readDataPalette(buf, PaletteType.CHUNK);
        DataPalette biomePalette = readDataPalette(buf, PaletteType.BIOME);
        return new ChunkSection(blockCount, chunkPalette, biomePalette);
    }

    public static void writeChunkSection(ByteBuf buf, ChunkSection section) {
        buf.writeShort(section.getBlockCount());
        writeDataPalette(buf, section.getChunkData());
        writeDataPalette(buf, section.getBiomeData());
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(ByteBuf buf, E[] values, Class<E> enumType) {
        BitSet bitSet = readFixedBitSet(buf, values.length);
        EnumSet<E> set = EnumSet.noneOf(enumType);

        for (int i = 0; i < values.length; i++) {
            if (bitSet.get(i)) {
                set.add(values[i]);
            }
        }

        return set;
    }

    public static <E extends Enum<E>> void writeEnumSet(ByteBuf buf, EnumSet<E> enumSet, E[] values) {
        BitSet bitSet = new BitSet(values.length);

        for (int i = 0; i < values.length; i++) {
            bitSet.set(i, enumSet.contains(values[i]));
        }

        writeFixedBitSet(buf, bitSet, values.length);
    }

    public static BitSet readFixedBitSet(ByteBuf buf, int length) {
        byte[] bytes = new byte[-Math.floorDiv(-length, 8)];
        buf.readBytes(bytes);
        return BitSet.valueOf(bytes);
    }

    public static void writeFixedBitSet(ByteBuf buf, BitSet bitSet, int length) {
        if (bitSet.length() > length) {
            throw new IllegalArgumentException("BitSet is larger than expected size (" + bitSet.length() + " > " + length + ")");
        } else {
            byte[] bytes = bitSet.toByteArray();
            buf.writeBytes(Arrays.copyOf(bytes, -Math.floorDiv(-length, 8)));
        }
    }

    public static GameProfile.Property readProperty(ByteBuf buf) {
        String name = readString(buf);
        String value = readString(buf);
        String signature = readNullable(buf, MinecraftTypes::readString);
        return new GameProfile.Property(name, value, signature);
    }

    public static void writeProperty(ByteBuf buf, GameProfile.Property property) {
        writeString(buf, property.getName());
        writeString(buf, property.getValue());
        writeNullable(buf, property.getSignature(), MinecraftTypes::writeString);
    }

    public static <T> T readById(ByteBuf buf, IntFunction<T> registry, Function<ByteBuf, T> custom) {
        int id = readVarInt(buf);
        if (id == 0) {
            return custom.apply(buf);
        }
        return registry.apply(id - 1);
    }

    public static CustomSound readSoundEvent(ByteBuf buf) {
        String name = readString(buf);
        boolean isNewSystem = buf.readBoolean();
        return new CustomSound(name, isNewSystem, isNewSystem ? buf.readFloat() : 16.0F);
    }

    public static void writeSoundEvent(ByteBuf buf, Sound soundEvent) {
        writeString(buf, soundEvent.getName());
        buf.writeBoolean(soundEvent.isNewSystem());
        if (soundEvent.isNewSystem()) {
            buf.writeFloat(soundEvent.getRange());
        }
    }
}
