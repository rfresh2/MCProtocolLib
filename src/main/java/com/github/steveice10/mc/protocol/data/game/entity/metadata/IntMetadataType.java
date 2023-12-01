package com.github.steveice10.mc.protocol.data.game.entity.metadata;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.type.IntEntityMetadata;
import io.netty.buffer.ByteBuf;

import java.io.UncheckedIOException;

public class IntMetadataType extends MetadataType<Integer> {
    private final IntReader primitiveReader;
    private final IntWriter primitiveWriter;
    private final IntEntityMetadataFactory primitiveFactory;

    protected IntMetadataType(IntReader reader, IntWriter writer, IntEntityMetadataFactory metadataFactory) {
        super(reader, writer, metadataFactory);

        this.primitiveReader = reader;
        this.primitiveWriter = writer;
        this.primitiveFactory = metadataFactory;
    }

    @Override
    public EntityMetadata<Integer, IntMetadataType> readMetadata(MinecraftCodecHelper helper, ByteBuf input, int id) throws UncheckedIOException {
        return this.primitiveFactory.createPrimitive(id, this, this.primitiveReader.readPrimitive(helper, input));
    }

    public void writeMetadataPrimitive(MinecraftCodecHelper helper, ByteBuf output, int value) throws UncheckedIOException {
        this.primitiveWriter.writePrimitive(helper, output, value);
    }

    @FunctionalInterface
    public interface IntReader extends Reader<Integer> {
        int readPrimitive(MinecraftCodecHelper helper, ByteBuf input) throws UncheckedIOException;

        @Deprecated
        @Override
        default Integer read(MinecraftCodecHelper helper, ByteBuf input) throws UncheckedIOException {
            return this.readPrimitive(helper, input);
        }
    }

    @FunctionalInterface
    public interface IntWriter extends Writer<Integer> {
        void writePrimitive(MinecraftCodecHelper helper, ByteBuf output, int value) throws UncheckedIOException;

        @Deprecated
        @Override
        default void write(MinecraftCodecHelper helper, ByteBuf output, Integer value) throws UncheckedIOException {
            this.writePrimitive(helper, output, value);
        }
    }

    @FunctionalInterface
    public interface IntEntityMetadataFactory extends EntityMetadataFactory<Integer> {
        IntEntityMetadata createPrimitive(int id, IntMetadataType type, int value);

        @Deprecated
        @Override
        default EntityMetadata<Integer, IntMetadataType> create(int id, MetadataType<Integer> type, Integer value) {
            throw new UnsupportedOperationException("Unsupported read method! Use primitive createPrimitive!");
        }
    }
}
