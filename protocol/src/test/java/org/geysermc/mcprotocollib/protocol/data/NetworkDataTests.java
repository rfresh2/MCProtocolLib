package org.geysermc.mcprotocollib.protocol.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Miscellaneous tests for reading and writing classes to/from the network
 */
public class NetworkDataTests {

    @Test
    public void testEffects() {
        MinecraftCodecHelper helper = MinecraftCodecHelper.INSTANCE;
        for (Effect effect : Effect.VALUES) {
            ByteBuf buf = Unpooled.buffer();
            helper.writeEffect(buf, effect);

            assertEquals(effect, helper.readEffect(buf));
        }
    }
}
