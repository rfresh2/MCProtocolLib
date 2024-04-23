package com.github.steveice10.mc.protocol.data.game.item.component;

import com.github.steveice10.mc.protocol.data.game.item.component.type.IntDataComponent;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.UncheckedIOException;

public class IntComponentType extends DataComponentType<Integer> {
    protected final IntReader primitiveReader;
    protected final IntWriter primitiveWriter;
    protected final IntDataComponentFactory primitiveFactory;

    protected IntComponentType(IntReader reader, IntWriter writer, IntDataComponentFactory metadataFactory) {
        super(reader, writer, metadataFactory);

        this.primitiveReader = reader;
        this.primitiveWriter = writer;
        this.primitiveFactory = metadataFactory;
    }

    @Override
    public DataComponent<Integer, IntComponentType> readDataComponent(ItemCodecHelper helper, ByteBuf input) throws UncheckedIOException {
        return this.primitiveFactory.createPrimitive(this, this.primitiveReader.readPrimitive(helper, input));
    }

    public void writeDataComponentPrimitive(ItemCodecHelper helper, ByteBuf output, int value) throws UncheckedIOException {
        this.primitiveWriter.writePrimitive(helper, output, value);
    }

    @FunctionalInterface
    public interface IntReader extends Reader<Integer> {
        int readPrimitive(ItemCodecHelper helper, ByteBuf input) throws UncheckedIOException;

        @Deprecated
        @Override
        default Integer read(ItemCodecHelper helper, ByteBuf input) throws UncheckedIOException {
            return this.readPrimitive(helper, input);
        }
    }

    @FunctionalInterface
    public interface IntWriter extends Writer<Integer> {
        void writePrimitive(ItemCodecHelper helper, ByteBuf output, int value) throws UncheckedIOException;

        @Deprecated
        @Override
        default void write(ItemCodecHelper helper, ByteBuf output, Integer value) throws UncheckedIOException {
            this.writePrimitive(helper, output, value);
        }
    }

    @FunctionalInterface
    public interface IntDataComponentFactory extends DataComponentFactory<Integer> {
        IntDataComponent createPrimitive(IntComponentType type, int value);

        @Deprecated
        @Override
        default DataComponent<Integer, IntComponentType> create(DataComponentType<Integer> type, Integer value) {
            throw new UnsupportedOperationException("Unsupported read method! Use primitive createPrimitive!");
        }
    }
}
