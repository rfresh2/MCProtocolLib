package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.viaversion.nbt.io.MNBTIO;
import com.viaversion.nbt.tag.CompoundTag;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
import org.geysermc.mcprotocollib.protocol.packet.PacketTest;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.Map;

public class ClientboundLevelChunkWithLightPacketTest extends PacketTest {
    @BeforeEach
    public void setup() throws IOException {
        var nbt = new CompoundTag(Map.of("HeightMaps", new CompoundTag()));
        MinecraftConstants.CHUNK_SECTION_COUNT_PROVIDER = () -> 0;
        this.setPackets(
                new ClientboundLevelChunkWithLightPacket(0, 0,
                                                         new ChunkSection[0], MNBTIO.write(nbt, false), new BlockEntityInfo[0],
                                                         new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), Collections.emptyList(), Collections.emptyList())
                ),
                new ClientboundLevelChunkWithLightPacket(1, 1,
                                                         new ChunkSection[0], MNBTIO.write(nbt, false), new BlockEntityInfo[] {
                        new BlockEntityInfo(1, 0, 1, BlockEntityType.CHEST, null)
                }, new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), Collections.emptyList(), Collections.emptyList())
                )
        );
    }
}
