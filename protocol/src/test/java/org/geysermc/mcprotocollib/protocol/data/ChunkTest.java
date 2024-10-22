package org.geysermc.mcprotocollib.protocol.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.Palette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.SingletonPalette;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkTest {
    private static final Logger log = LoggerFactory.getLogger(ChunkTest.class);
    private final List<ChunkSection> chunkSectionsToTest = new ArrayList<>();

    @BeforeEach
    public void setup() {
        chunkSectionsToTest.add(new ChunkSection());

        ChunkSection section = new ChunkSection();
        section.setBlock(0, 0, 0, 10);
        chunkSectionsToTest.add(section);

        SingletonPalette singletonPalette = new SingletonPalette(20);
        DataPalette dataPalette = new DataPalette(singletonPalette, null, PaletteType.CHUNK);
        DataPalette biomePalette = new DataPalette(singletonPalette, null, PaletteType.BIOME);
        section = new ChunkSection(4096, dataPalette, biomePalette);
        chunkSectionsToTest.add(section);
    }

    @Test
    public void testChunkSectionEncoding() {
        MinecraftCodecHelper helper = MinecraftCodecHelper.INSTANCE;
        for (ChunkSection section : chunkSectionsToTest) {
            ByteBuf buf = Unpooled.buffer();
            helper.writeChunkSection(buf, section);
            ChunkSection decoded;
            try {
                decoded = helper.readChunkSection(buf);
            } catch (Exception e) {
                log.error(section.toString(), e);
                throw e;
            }
            assertEquals(section.getBlockCount(), decoded.getBlockCount(), "Block count does not match original: " + section.getBlockCount() + " vs " + decoded.getBlockCount());
            Palette expectedBiomePalette = section.getBiomeData().getPalette();
            Palette biomePalette = decoded.getBiomeData().getPalette();
            assertEquals(expectedBiomePalette.size(), biomePalette.size(), "Biome palette size does not match original: " + expectedBiomePalette.size() + " vs " + biomePalette.size());
            for (int i = 0; i < expectedBiomePalette.size(); i++) {
                int expectedState = expectedBiomePalette.idToState(i);
                int state = biomePalette.idToState(i);
                assertEquals(expectedState, state, "Biome palette state does not match original: " + expectedState + " vs " + state);
            }
            assertEquals(section.getChunkData().getPalette().size(), decoded.getChunkData().getPalette().size(), "Chunk palette size does not match original: " + section.getChunkData().getPalette().size() + " vs " + decoded.getChunkData().getPalette().size());
            Palette expectedChunkPalette = section.getChunkData().getPalette();
            Palette chunkPalette = decoded.getChunkData().getPalette();
            for (int i = 0; i < expectedChunkPalette.size(); i++) {
                int expectedState = expectedChunkPalette.idToState(i);
                int state = chunkPalette.idToState(i);
                assertEquals(expectedState, state, "Chunk palette state does not match original: " + expectedState + " vs " + state);
            }
            BitStorage expectedBiomeStorage = section.getBiomeData().getStorage();
            BitStorage biomeStorage = decoded.getBiomeData().getStorage();
            if (expectedBiomeStorage == null) {
                assertNull(biomeStorage, "Biome storage is not null");
            } else {
                assertEquals(expectedBiomeStorage.getSize(), biomeStorage.getSize(), "Biome storage size does not match original: " + expectedBiomeStorage.getSize() + " vs " + biomeStorage.getSize());
                assertEquals(expectedBiomeStorage.getBitsPerEntry(), biomeStorage.getBitsPerEntry(), "Biome storage bits per entry does not match original: " + expectedBiomeStorage.getBitsPerEntry() + " vs " + biomeStorage.getBitsPerEntry());
                for (int i = 0; i < expectedBiomeStorage.getSize(); i++) {
                    long expectedValue = expectedBiomeStorage.get(i);
                    long value = biomeStorage.get(i);
                    assertEquals(expectedValue, value, "Biome storage value does not match original: " + expectedValue + " vs " + value);
                }
            }

            BitStorage expectedChunkStorage = section.getChunkData().getStorage();
            BitStorage chunkStorage = decoded.getChunkData().getStorage();
            if (expectedChunkStorage == null) {
                assertNull(chunkStorage, "Chunk storage is not null");
            } else {
                assertEquals(expectedChunkStorage.getSize(), chunkStorage.getSize(), "Chunk storage size does not match original: " + expectedChunkStorage.getSize() + " vs " + chunkStorage.getSize());
                assertEquals(expectedChunkStorage.getBitsPerEntry(), chunkStorage.getBitsPerEntry(), "Chunk storage bits per entry does not match original: " + expectedChunkStorage.getBitsPerEntry() + " vs " + chunkStorage.getBitsPerEntry());
                for (int i = 0; i < expectedChunkStorage.getSize(); i++) {
                    long expectedValue = expectedChunkStorage.get(i);
                    long value = chunkStorage.get(i);
                    assertEquals(expectedValue, value, "Chunk storage value does not match original: " + expectedValue + " vs " + value);
                }
            }
        }
    }

    @Test
    public void testDeepCopy() {
        for (ChunkSection section : chunkSectionsToTest) {
            ChunkSection copy = new ChunkSection(section);
            assertEquals(section, copy, "Deep copy does not match original: " + section + " vs " + copy);

            copy.setBlock(1, 1, 1, 10);
            assertNotEquals(section, copy, "Deep copy is not deep: " + section + " vs " + copy);
        }
    }
}
