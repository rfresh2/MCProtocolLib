package com.github.steveice10.mc.protocol;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import lombok.Data;
import org.junit.Test;

import java.util.*;

import static com.github.steveice10.mc.protocol.data.game.world.block.BlockState.of;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ChunkTest {
    @Test
    public void setAndGetTests() {
        final Chunk chunk = new Chunk(true);
        final Random random = new Random();
        final Set<BlockAtPos> blockAtPosList = new HashSet<>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    BlockState state = new BlockState(random.nextInt(200), 0);
                    chunk.getBlocks().set(i, j, k, state);
                    blockAtPosList.add(new BlockAtPos(i, j, k, state));
                    assertEquals(chunk.getBlocks().get(i, j, k), state);
                    for (BlockAtPos blockAtPos : blockAtPosList) {
                        assertEquals(chunk.getBlocks().get(blockAtPos.x, blockAtPos.y, blockAtPos.z), blockAtPos.blockState);
                    }
                }
            }
        }

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    BlockState state = new BlockState(random.nextInt(200), 0);
                    chunk.getBlocks().set(i, j, k, state);
                    BlockAtPos blockAtPos1 = new BlockAtPos(i, j, k, state);
                    blockAtPosList.removeIf(blockAtPos -> blockAtPos.getX() == blockAtPos1.getX() && blockAtPos.getY() == blockAtPos1.getY() && blockAtPos.getZ() == blockAtPos1.getZ());
                    blockAtPosList.add(blockAtPos1);
                    assertEquals(chunk.getBlocks().get(i, j, k), state);
                    for (BlockAtPos blockAtPos : blockAtPosList) {
                        assertEquals(chunk.getBlocks().get(blockAtPos.x, blockAtPos.y, blockAtPos.z), blockAtPos.blockState);
                    }
                }
            }
        }
    }

    private static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

//    @Test
    public void tryingToReproduceAState() {
        final Chunk chunk = new Chunk(true);
        chunk.getBlocks().setBitsPerEntry(6);
        final List<BlockState> states = new ArrayList<>();
        states.addAll(asList(of(0, 0), of(2, 0), of(159, 11), of(9, 1), of(9, 2), of(9, 3), of(9, 4), of(9, 5), of(9, 6),
                of(9, 7), of(9, 0), of(83,13), of(83, 0), of(83, 14), of(138, 0), of(83, 7), of(83, 10), of(83, 5), of(83, 2),
                of(83, 15), of(83, 4), of(83, 3), of(33, 3), of(83, 1), of(33, 2), of(55, 0), of(218, 3), of(218, 2),
                of(83, 9), of(83, 11), of(83, 8), of(83, 6), of(20, 0)));
        chunk.getBlocks().setStates(states);
        long[] data = new long[]{0, 0, 0, 0, 0, 0, 1171226243996323905L, 511247631101477124L, 2896101637926486162L, 8217099622298439939L, 4684887384405378328L, 292805461487454224L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, -9076969306247397376L, 585610922974904352L, -4319171038531239157L, 1294143556, 0, 0, -8338198604502007808L, 5270702868021786820L, 2342434895806603394L, 136348168, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2342434895806603394L,
                -7612942000037264888L, 6441720152723947877L, 8796093022208L, 0, 0, 0, 864691179994742784L, 3458777707960862723L, -8784269397739862504L, -9076969304611219359L, 585610922974904352L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -7613051949836560807L,
                -7320136537254504091L, 7612941998673756582L, 8796093022208L, 0, 0, 0, 0, 2048, -5270617054029957413L, 6441720152860318573L, 7320136537186304406L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2342443691899625602L, -9076969306111049208L,
                585610922974904352L, 2342434895806603394L, -9076969306111049208L, 585610922974906400L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1171226243996323905L, 511247631101477124L, 2896101637926486162L, 8217099622298439939L,
                4684887384405378328L, 292805461487454224L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -9076969306247397376L, 585610922974904352L, -4392072901752212787L, 818089008, 0, 0, 814311630224490496L, 5531559650719971779L, 2342434895806603394L,
                136348168, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2342434895806603394L, -7612942000037264888L, 6441720152723947877L, -4392126740500054016L, 818085936, 0, 0, 0, 844428201691140L, -8784269397739862504L, -9076969304611219359L,
                585610922974904352L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -7613051949836560807L, -7320136537254504091L, 7612941998673756582L, 8796093022208L, 0, 0, 0, 0, 2048, -5270617054029957413L, 6441720152860318573L, 7320136537186304406L, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 585470185486551072L, 2342443691899625602L, -9076969306111049208L, 585610922974906400L, 2342443691899625602L, -9076969306111081976L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        chunk.getBlocks().getStorage().setData(data);
        chunk.getBlocks().getStorage().setSize(4096);
        chunk.getBlocks().getStorage().setMaxEntryValue(63);
        long[] clone = data.clone();
        chunk.getBlocks().set(-165 & 0xF, 115 & 0xF, 482 & 0xF, new BlockState(55, 0));
        assertArrayEquals(clone, chunk.getBlocks().getStorage().getData());
        assertEquals(chunk.getBlocks().get(-166 & 0xF, 115 & 0xF, 482 & 0xF), new BlockState(55, 0));

        chunk.getBlocks().set(-166 & 0xF, 115 & 0xF, 482 & 0xF, new BlockState(55, 0));
        assertEquals(chunk.getBlocks().get(-165 & 0xF, 115 & 0xF, 482 & 0xF), new BlockState(55, 0));
        assertEquals(chunk.getBlocks().get(-166 & 0xF, 115 & 0xF, 482 & 0xF), new BlockState(55, 0));
    }

    @Data
    private static class BlockAtPos {
        private final int x;
        private final int y;
        private final int z;
        private final BlockState blockState;
    }
}
