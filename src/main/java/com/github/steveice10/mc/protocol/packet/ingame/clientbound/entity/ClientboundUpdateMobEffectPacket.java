package com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.data.game.entity.Effect;
import com.github.steveice10.opennbt.mini.MNBT;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Data
@With
@AllArgsConstructor
public class ClientboundUpdateMobEffectPacket implements MinecraftPacket {
    private static final int FLAG_AMBIENT = 0x01;
    private static final int FLAG_SHOW_PARTICLES = 0x02;

    private final int entityId;
    private final @NonNull Effect effect;
    private final int amplifier;
    private final int duration;
    private final boolean ambient;
    private final boolean showParticles;
    private final @Nullable MNBT factorData;

    public ClientboundUpdateMobEffectPacket(ByteBuf in, MinecraftCodecHelper helper) throws IOException {
        this.entityId = helper.readVarInt(in);
        this.effect = helper.readEffect(in);
        this.amplifier = in.readByte();
        this.duration = helper.readVarInt(in);

        int flags = in.readByte();
        this.ambient = (flags & FLAG_AMBIENT) != 0;
        this.showParticles = (flags & FLAG_SHOW_PARTICLES) != 0;
        if (in.readBoolean()) {
            this.factorData = helper.readMNBT(in);
        } else {
            this.factorData = null;
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) throws IOException {
        helper.writeVarInt(out, this.entityId);
        helper.writeEffect(out, this.effect);
        out.writeByte(this.amplifier);
        helper.writeVarInt(out, this.duration);

        int flags = 0;
        if (this.ambient) {
            flags |= FLAG_AMBIENT;
        }

        if (this.showParticles) {
            flags |= FLAG_SHOW_PARTICLES;
        }

        out.writeByte(flags);
        out.writeBoolean(this.factorData != null);
        if (this.factorData != null) {
            helper.writeMNBT(out, this.factorData);
        }
    }
}
