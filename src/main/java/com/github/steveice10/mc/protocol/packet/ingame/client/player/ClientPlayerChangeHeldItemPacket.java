package com.github.steveice10.mc.protocol.packet.ingame.client.player;

import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;

import java.io.IOException;

public class ClientPlayerChangeHeldItemPacket extends MinecraftPacket {
    private int slot;

    public ClientPlayerChangeHeldItemPacket(NetInput in) throws IOException {
        this.slot = in.readShort();
    }

    public ClientPlayerChangeHeldItemPacket(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeShort(this.slot);
    }
}
