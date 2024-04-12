package org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.*;

import java.util.Map;

@Data
@With
public class ServerboundContainerClickPacket implements MinecraftPacket {
    public static final int CLICK_OUTSIDE_NOT_HOLDING_SLOT = -999;

    private final int containerId;
    private final int stateId;
    private final int slot;
    private final byte param;
    private final byte actionType;
    private final @Nullable ItemStack carriedItem;
    private final @NonNull Int2ObjectMap<@Nullable ItemStack> changedSlots;

    public ServerboundContainerClickPacket(int containerId, int stateId, int slot,
                                           @NonNull ContainerActionType action, @NonNull ContainerAction param,
                                           @Nullable ItemStack carriedItem, @NonNull Map<Integer, @Nullable ItemStack> changedSlots) {
        this(containerId, stateId, slot, action, param, carriedItem, new Int2ObjectOpenHashMap<>(changedSlots));
    }

    public ServerboundContainerClickPacket(int containerId, int stateId, int slot,
                                           @NonNull ContainerActionType action, @NonNull ContainerAction param,
                                           @Nullable ItemStack carriedItem, @NonNull Int2ObjectMap<@Nullable ItemStack> changedSlots) {
        if ((param == DropItemAction.LEFT_CLICK_OUTSIDE_NOT_HOLDING || param == DropItemAction.RIGHT_CLICK_OUTSIDE_NOT_HOLDING)
            && slot != -CLICK_OUTSIDE_NOT_HOLDING_SLOT) {
            throw new IllegalArgumentException("Slot must be " + CLICK_OUTSIDE_NOT_HOLDING_SLOT
                                                   + " with param LEFT_CLICK_OUTSIDE_NOT_HOLDING or RIGHT_CLICK_OUTSIDE_NOT_HOLDING");
        }
        int paramId = param.getId();
        if (action == ContainerActionType.DROP_ITEM) {
            paramId %= 2;
        }
        byte paramByte = (byte) paramId;
        byte actionByte = (byte) action.ordinal();
        this.containerId = containerId;
        this.stateId = stateId;
        this.slot = slot;
        this.param = paramByte;
        this.actionType = actionByte;
        this.carriedItem = carriedItem;
        this.changedSlots = changedSlots;
    }

    public ServerboundContainerClickPacket(final int containerId, final int stateId, final int slot, final byte param, final byte actionType, final ItemStack carriedItem, final @NonNull Int2ObjectMap<ItemStack> changedSlots) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.slot = slot;
        this.param = param;
        this.actionType = actionType;
        this.carriedItem = carriedItem;
        this.changedSlots = changedSlots;
    }

    public ServerboundContainerClickPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.containerId = in.readByte();
        this.stateId = helper.readVarInt(in);
        this.slot = in.readShort();
        this.param = in.readByte();
        this.actionType = in.readByte();
        int changedItemsSize = helper.readVarInt(in);
        this.changedSlots = new Int2ObjectOpenHashMap<>(changedItemsSize);
        for (int i = 0; i < changedItemsSize; i++) {
            int key = in.readShort();
            ItemStack value = helper.readItemStack(in);
            this.changedSlots.put(key, value);
        }

        this.carriedItem = helper.readItemStack(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        out.writeByte(this.containerId);
        helper.writeVarInt(out, this.stateId);
        out.writeShort(this.slot);
        out.writeByte(this.param);
        out.writeByte(this.actionType);
        helper.writeVarInt(out, this.changedSlots.size());
        for (Int2ObjectMap.Entry<ItemStack> pair : this.changedSlots.int2ObjectEntrySet()) {
            out.writeShort(pair.getIntKey());
            helper.writeItemStack(out, pair.getValue());
        }

        helper.writeItemStack(out, this.carriedItem);
    }

    public ContainerActionType getActionType() {
        return ContainerActionType.from(actionType);
    }

    public ContainerAction getActionParam() {
        var action = getActionType();
        if (action == ContainerActionType.CLICK_ITEM) {
            return ClickItemAction.from(param);
        } else if (action == ContainerActionType.SHIFT_CLICK_ITEM) {
            return ShiftClickItemAction.from(param);
        } else if (action == ContainerActionType.MOVE_TO_HOTBAR_SLOT) {
            return MoveToHotbarAction.from(param);
        } else if (action == ContainerActionType.CREATIVE_GRAB_MAX_STACK) {
            return CreativeGrabAction.from(param);
        } else if (action == ContainerActionType.DROP_ITEM) {
            return DropItemAction.from(param + (this.slot != -999 ? 2 : 0));
        } else if (action == ContainerActionType.SPREAD_ITEM) {
            return SpreadItemAction.from(param);
        } else if (action == ContainerActionType.FILL_STACK) {
            return FillStackAction.from(param);
        } else {
            throw new IllegalStateException();
        }
    }
}
