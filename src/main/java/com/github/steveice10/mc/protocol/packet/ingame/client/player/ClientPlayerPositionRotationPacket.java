package com.github.steveice10.mc.protocol.packet.ingame.client.player;

import com.github.steveice10.packetlib.io.NetInput;

import java.io.IOException;

public class ClientPlayerPositionRotationPacket extends ClientPlayerMovementPacket {
    protected ClientPlayerPositionRotationPacket() {
        this.pos = true;
        this.rot = true;
    }

    public ClientPlayerPositionRotationPacket(NetInput in) throws IOException {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();
        this.onGround = in.readBoolean();
        this.pos = true;
        this.rot = true;
    }

    public ClientPlayerPositionRotationPacket(boolean onGround, double x, double y, double z, float yaw, float pitch) {
        super(onGround);
        this.pos = true;
        this.rot = true;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
