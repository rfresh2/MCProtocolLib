package org.geysermc.mcprotocollib.protocol.data;

import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChunkSectionTest {
    // warning: modded servers with custom blocks can be even higher which WILL break a global palette
    private static final int MAX_BLOCK_STATE_ID_1_21 = 26684;

    @Test
    public void testLargeBlockStateId() {
        final ChunkSection section = new ChunkSection();
        writeNewBlockStates(section, 0, MAX_BLOCK_STATE_ID_1_21);
        // we now have a global palette

        try {
            section.setBlock(0, 0, 0, 24000);
        } catch (final Throwable e) {
            Assertions.fail(e);
        }

    }

    @Test
    public void testMutation() {
        final ChunkSection section = new ChunkSection();
        int nextBlockStateId = 0;

        try {
            for (int i = 0; i < 16; i++) {
                nextBlockStateId = writeNewBlockStates(section, nextBlockStateId++, MAX_BLOCK_STATE_ID_1_21);
            }
        } catch (final Throwable e) {
            Assertions.fail(e);
        }
    }

    private int writeNewBlockStates(ChunkSection section, int startBlockStateId, int maxBlockStateId) {
        int nextBlockStateId = startBlockStateId++;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    if (nextBlockStateId >= maxBlockStateId) {
                        nextBlockStateId = 0;
                    }
                    section.setBlock(x, y, z, nextBlockStateId++);
                }
            }
        }
        return nextBlockStateId;
    }
}
