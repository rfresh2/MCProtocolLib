package com.github.steveice10.mc.protocol.packet.status.client;

import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;

import java.io.IOException;

public class StatusQueryPacket extends MinecraftPacket {
    public StatusQueryPacket() {
    }

    public StatusQueryPacket(NetInput in) {

    }

    @Override
    public void write(NetOutput out) throws IOException {
    }
}
