package com.github.steveice10.mc.protocol.packet.ingame.server.entity.player;

import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;

import java.io.IOException;

public class ServerPlayerChangeHeldItemPacket extends MinecraftPacket {
    private int slot;

    public ServerPlayerChangeHeldItemPacket(NetInput in) throws IOException {
        this.slot = in.readByte();
    }

    public ServerPlayerChangeHeldItemPacket(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeByte(this.slot);
    }
}
