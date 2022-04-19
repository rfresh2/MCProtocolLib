package com.github.steveice10.mc.protocol.packet.ingame.client;

import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;

import java.io.IOException;

public class ClientPluginMessagePacket extends MinecraftPacket {
    private String channel;
    private byte data[];

    public ClientPluginMessagePacket(NetInput in) throws IOException {
        this.channel = in.readString();
        this.data = in.readBytes(in.available());
    }

    public ClientPluginMessagePacket(String channel, byte data[]) {
        this.channel = channel;
        this.data = data;
    }

    public String getChannel() {
        return this.channel;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeString(this.channel);
        out.writeBytes(this.data);
    }
}
