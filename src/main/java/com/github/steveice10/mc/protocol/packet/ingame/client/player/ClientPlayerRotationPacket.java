package com.github.steveice10.mc.protocol.packet.ingame.client.player;

import com.github.steveice10.packetlib.io.NetInput;

import java.io.IOException;

public class ClientPlayerRotationPacket extends ClientPlayerMovementPacket {
    protected ClientPlayerRotationPacket() {
        this.rot = true;
    }

    public ClientPlayerRotationPacket(NetInput in) throws IOException {
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();
        this.onGround = in.readBoolean();
        this.rot = true;
    }

    public ClientPlayerRotationPacket(boolean onGround, float yaw, float pitch) {
        super(onGround);
        this.rot = true;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
