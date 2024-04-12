package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
import org.geysermc.mcprotocollib.protocol.packet.PacketTest;
import org.junit.jupiter.api.BeforeEach;

import java.util.BitSet;
import java.util.Collections;
import java.util.Map;

public class ClientboundLevelChunkWithLightPacketTest extends PacketTest {
    @BeforeEach
    public void setup() {
        var nbt = new CompoundTag(Map.of("HeightMaps", new CompoundTag()));
        this.setPackets(
                new ClientboundLevelChunkWithLightPacket(0, 0,
                        new byte[0], nbt, new BlockEntityInfo[0],
                        new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), Collections.emptyList(), Collections.emptyList())
                ),
                new ClientboundLevelChunkWithLightPacket(1, 1,
                        new byte[256], nbt, new BlockEntityInfo[] {
                        new BlockEntityInfo(1, 0, 1, BlockEntityType.CHEST, null)
                }, new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), Collections.emptyList(), Collections.emptyList())
                )
        );
    }
}
