package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.WeightedList;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.Particle;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.BuiltinSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.CustomSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;
import org.jetbrains.annotations.Nullable;

@Data
@With
@AllArgsConstructor
public class ClientboundExplodePacket implements MinecraftPacket {
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final float radius;
    private final int blockCount;
    private final @Nullable Vector3d playerKnockback;
    private final @NonNull Particle explosionParticle;
    private final @NonNull Sound explosionSound;
    private final @NonNull WeightedList<BlockParticleInfo> blockParticles;

    public ClientboundExplodePacket(ByteBuf in) {
        this.centerX = in.readDouble();
        this.centerY = in.readDouble();
        this.centerZ = in.readDouble();
        this.radius = in.readFloat();
        this.blockCount = in.readInt();
        this.playerKnockback = MinecraftTypes.readNullable(in, (input) -> Vector3d.from(in.readDouble(), in.readDouble(), in.readDouble()));
        this.explosionParticle = MinecraftTypes.readParticle(in);
        this.explosionSound = MinecraftTypes.readById(in, BuiltinSound::from, MinecraftTypes::readSoundEvent);
        this.blockParticles = new WeightedList<>(in, BlockParticleInfo::new);
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeDouble(this.centerX);
        out.writeDouble(this.centerY);
        out.writeDouble(this.centerZ);
        out.writeFloat(radius);
        out.writeInt(blockCount);
        MinecraftTypes.writeNullable(out, this.playerKnockback, (output, vector) -> {
            out.writeDouble(vector.getX());
            out.writeDouble(vector.getY());
            out.writeDouble(vector.getZ());
        });
        MinecraftTypes.writeParticle(out, this.explosionParticle);
        if (this.explosionSound instanceof CustomSound) {
            MinecraftTypes.writeVarInt(out, 0);
            MinecraftTypes.writeSoundEvent(out, this.explosionSound);
        } else {
            MinecraftTypes.writeVarInt(out, ((BuiltinSound) this.explosionSound).ordinal() + 1);
        }
        blockParticles.write(out, BlockParticleInfo::write);
    }

    public record BlockParticleInfo(Particle particle, float scaling, float speed) {

        public BlockParticleInfo(ByteBuf in) {
            this(MinecraftTypes.readParticle(in), in.readFloat(), in.readFloat());
        }

        public void write(ByteBuf out) {
            MinecraftTypes.writeParticle(out, particle);
            out.writeFloat(scaling);
            out.writeFloat(speed);
        }
    }
}
