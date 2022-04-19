package com.github.steveice10.mc.protocol.packet.status.server;

import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;

import java.io.IOException;

public class StatusPongPacket extends MinecraftPacket {
    private long time;

    public StatusPongPacket(NetInput in) throws IOException {
        this.time = in.readLong();
    }

    public StatusPongPacket(long time) {
        this.time = time;
    }

    public long getPingTime() {
        return this.time;
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeLong(this.time);
    }
}
