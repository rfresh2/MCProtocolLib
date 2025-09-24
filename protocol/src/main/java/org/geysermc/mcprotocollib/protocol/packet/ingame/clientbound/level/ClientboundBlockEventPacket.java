package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.WobbleStyle;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.BellValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.BellValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.BlockValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.BlockValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.ChestValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.ChestValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.DecoratedPotValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.DecoratedPotValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.EndGatewayValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.EndGatewayValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.GenericBlockValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.GenericBlockValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.MobSpawnerValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.MobSpawnerValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.NoteBlockValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.NoteBlockValueType;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.PistonValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.PistonValueType;

@Data
@With
@AllArgsConstructor
public class ClientboundBlockEventPacket implements MinecraftPacket {
    // Do we really want these hardcoded values?
    private static final int NOTE_BLOCK = 109;
    private static final int STICKY_PISTON = 128;
    private static final int PISTON = 138;
    private static final int MOB_SPAWNER = 197;
    private static final int CHEST = 200;
    private static final int ENDER_CHEST = 381;
    private static final int TRAPPED_CHEST = 450;
    private static final int END_GATEWAY = 647;
    private static final int SHULKER_BOX_LOWER = 657;
    private static final int SHULKER_BOX_HIGHER = 673;
    private static final int BELL = 828;
    private static final int DECORATED_POT = 1127;

    private final int x;
    private final int y;
    private final int z;
    private final int rawType;
    private final int rawValue;
    private final int blockId;

    public ClientboundBlockEventPacket(ByteBuf in) {
        var position = in.readLong();
        this.x = MinecraftTypes.decodePositionX(position);
        this.y = MinecraftTypes.decodePositionY(position);
        this.z = MinecraftTypes.decodePositionZ(position);
        this.rawType = in.readUnsignedByte();
        this.rawValue = in.readUnsignedByte();
        this.blockId = MinecraftTypes.readVarInt(in);
    }

    @Override
    public void serialize(ByteBuf out) {
        MinecraftTypes.writePosition(out, this.x, this.y, this.z);
        out.writeByte(rawType);
        out.writeByte(rawValue);
        MinecraftTypes.writeVarInt(out, this.blockId);
    }

    public BlockValueType getType() {
        if (this.blockId == NOTE_BLOCK) {
            return NoteBlockValueType.from(rawType);
        } else if (this.blockId == STICKY_PISTON || this.blockId == PISTON) {
            return PistonValueType.from(rawType);
        } else if (this.blockId == MOB_SPAWNER) {
            return MobSpawnerValueType.from(rawType - 1);
        } else if (this.blockId == CHEST || this.blockId == ENDER_CHEST || this.blockId == TRAPPED_CHEST
            || (this.blockId >= SHULKER_BOX_LOWER && this.blockId <= SHULKER_BOX_HIGHER)) {
            return ChestValueType.from(rawType - 1);
        } else if (this.blockId == END_GATEWAY) {
            return EndGatewayValueType.from(rawType - 1);
        } else if (this.blockId == BELL) {
            return BellValueType.from(rawType - 1);
        } else if (this.blockId == DECORATED_POT) {
            return DecoratedPotValueType.from(rawType - 1);
        } else {
            return GenericBlockValueType.from(rawType);
        }
    }

    public BlockValue getValue() {
        if (this.blockId == NOTE_BLOCK) {
            return new NoteBlockValue();
        } else if (this.blockId == STICKY_PISTON || this.blockId == PISTON) {
            return new PistonValue(Direction.from(Math.abs((rawValue & 7) % 6)));
        } else if (this.blockId == MOB_SPAWNER) {
            return new MobSpawnerValue();
        } else if (this.blockId == CHEST || this.blockId == ENDER_CHEST || this.blockId == TRAPPED_CHEST
            || (this.blockId >= SHULKER_BOX_LOWER && this.blockId <= SHULKER_BOX_HIGHER)) {
            return new ChestValue(rawValue);
        } else if (this.blockId == END_GATEWAY) {
            return new EndGatewayValue();
        } else if (this.blockId == BELL) {
            return new BellValue(Direction.from(Math.abs(rawValue % 6)));
        } else if (this.blockId == DECORATED_POT) {
            return new DecoratedPotValue(WobbleStyle.from(Math.abs(rawValue % 2)));
        } else {
            return new GenericBlockValue(rawValue);
        }
    }
}
