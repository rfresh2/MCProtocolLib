package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.ExplosionInteraction;
import org.geysermc.mcprotocollib.protocol.data.game.level.particle.Particle;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.BuiltinSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.CustomSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundExplodePacket implements MinecraftPacket {
    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final @NonNull List<Vector3i> exploded;
    private final float pushX;
    private final float pushY;
    private final float pushZ;
    private final @NonNull Particle smallExplosionParticles;
    private final @NonNull Particle largeExplosionParticles;
    private final @NonNull ExplosionInteraction blockInteraction;
    private final @NonNull Sound explosionSound;

    public ClientboundExplodePacket(ByteBuf in) {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.radius = in.readFloat();
        int dx = floorI(this.x);
        int dy = floorI(this.y);
        int dz = floorI(this.z);
        int length = MinecraftTypes.readVarInt(in);
        this.exploded = new ArrayList<>(length);
        for (int count = 0; count < length; count++) {
            this.exploded.add(Vector3i.from(
                in.readByte() + dx,
                in.readByte() + dy,
                in.readByte() + dz
            ));
        }

        this.pushX = in.readFloat();
        this.pushY = in.readFloat();
        this.pushZ = in.readFloat();
        this.blockInteraction = ExplosionInteraction.from(MinecraftTypes.readVarInt(in)); // different order than mojang fields
        this.smallExplosionParticles = MinecraftTypes.readParticle(in);
        this.largeExplosionParticles = MinecraftTypes.readParticle(in);
        this.explosionSound = MinecraftTypes.readById(in, BuiltinSound::from, MinecraftTypes::readSoundEvent);
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeFloat(this.radius);
        int dx = floorI(this.x);
        int dy = floorI(this.y);
        int dz = floorI(this.z);
        MinecraftTypes.writeVarInt(out, this.exploded.size());
        for (int i = 0; i < this.exploded.size(); i++) {
            Vector3i record = this.exploded.get(i);
            out.writeByte(record.getX() - dx);
            out.writeByte(record.getY() - dy);
            out.writeByte(record.getZ() - dz);
        }

        out.writeFloat(this.pushX);
        out.writeFloat(this.pushY);
        out.writeFloat(this.pushZ);
        MinecraftTypes.writeVarInt(out, this.blockInteraction.ordinal()); // different order than mojang fields
        MinecraftTypes.writeParticle(out, this.smallExplosionParticles);
        MinecraftTypes.writeParticle(out, this.largeExplosionParticles);
        if (this.explosionSound instanceof CustomSound) {
            MinecraftTypes.writeVarInt(out, 0);
            MinecraftTypes.writeSoundEvent(out, this.explosionSound);
        } else {
            MinecraftTypes.writeVarInt(out, ((BuiltinSound) this.explosionSound).ordinal() + 1);
        }
    }

    public int floorI(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }
}
