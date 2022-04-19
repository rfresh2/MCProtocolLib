package com.github.steveice10.mc.protocol.packet.ingame.client.player;

import com.github.steveice10.packetlib.io.NetInput;

import java.io.IOException;

public class ClientPlayerPositionPacket extends ClientPlayerMovementPacket {
    protected ClientPlayerPositionPacket() {
        this.pos = true;
    }

    public ClientPlayerPositionPacket(NetInput in) throws IOException {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.onGround = in.readBoolean();
        this.pos = true;
    }

    public ClientPlayerPositionPacket(boolean onGround, double x, double y, double z) {
        super(onGround);
        this.pos = true;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
