package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.Particle;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.BuiltinSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.CustomSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;

@Data
@With
@AllArgsConstructor
public class ClientboundExplodePacket implements MinecraftPacket {
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final boolean hasKnockback;
    private final double playerKnockbackX;
    private final double playerKnockbackY;
    private final double playerKnockbackZ;
    private final @NonNull Particle explosionParticle;
    private final @NonNull Sound explosionSound;

    public ClientboundExplodePacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.centerX = in.readDouble();
        this.centerY = in.readDouble();
        this.centerZ = in.readDouble();
        this.hasKnockback = in.readBoolean();
        if (this.hasKnockback) {
            this.playerKnockbackX = in.readDouble();
            this.playerKnockbackY = in.readDouble();
            this.playerKnockbackZ = in.readDouble();
        } else {
            this.playerKnockbackX = 0.0;
            this.playerKnockbackY = 0.0;
            this.playerKnockbackZ = 0.0;
        }
        this.explosionParticle = helper.readParticle(in);
        this.explosionSound = helper.readById(in, BuiltinSound::from, helper::readSoundEvent);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        out.writeDouble(this.centerX);
        out.writeDouble(this.centerY);
        out.writeDouble(this.centerZ);
        out.writeBoolean(this.hasKnockback);
        if (this.hasKnockback) {
            out.writeDouble(this.playerKnockbackX);
            out.writeDouble(this.playerKnockbackY);
            out.writeDouble(this.playerKnockbackZ);
        }
        helper.writeParticle(out, this.explosionParticle);
        if (this.explosionSound instanceof CustomSound) {
            helper.writeVarInt(out, 0);
            helper.writeSoundEvent(out, this.explosionSound);
        } else {
            helper.writeVarInt(out, ((BuiltinSound) this.explosionSound).ordinal() + 1);
        }
    }
}
