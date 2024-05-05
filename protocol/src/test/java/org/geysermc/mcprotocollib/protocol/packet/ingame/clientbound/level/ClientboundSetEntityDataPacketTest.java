package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.*;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.packet.PacketTest;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.OptionalInt;

import static java.util.Arrays.asList;

public class ClientboundSetEntityDataPacketTest extends PacketTest {

    @BeforeEach
    public void setup() {
        this.setPackets(
                new ClientboundSetEntityDataPacket(0, Collections.emptyList()),
                new ClientboundSetEntityDataPacket(20, asList(
                        new ObjectEntityMetadata<>(1, MetadataType.STRING, "Hello!")
                )),
                new ClientboundSetEntityDataPacket(2, asList(
                        new BooleanEntityMetadata(0, MetadataType.BOOLEAN, true),
                        new ByteEntityMetadata(4, MetadataType.BYTE, (byte) 45),
                        new IntEntityMetadata(2, MetadataType.INT, 555),
                        new FloatEntityMetadata(3, MetadataType.FLOAT, 3.0f),
                        new LongEntityMetadata(8, MetadataType.LONG, 123456789L),
                        new ObjectEntityMetadata<>(5, MetadataType.POSITION, Vector3i.from(0, 1, 0)),
                        new ObjectEntityMetadata<>(2, MetadataType.BLOCK_STATE, 60),
                        new ObjectEntityMetadata<>(6, MetadataType.DIRECTION, Direction.EAST),
                        new ObjectEntityMetadata<>(7, MetadataType.OPTIONAL_VARINT, OptionalInt.of(1038))
                )),
                new ClientboundSetEntityDataPacket(700, asList(
                        // Boxed variation test
                        new ObjectEntityMetadata<>(0, MetadataType.INT, 0),
                        new ObjectEntityMetadata<>(1, MetadataType.FLOAT, 1.0f)
                ))
        );
    }
}
