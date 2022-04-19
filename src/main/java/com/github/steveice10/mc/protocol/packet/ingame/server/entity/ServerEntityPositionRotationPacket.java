package com.github.steveice10.mc.protocol.packet.ingame.server.entity;

import com.github.steveice10.packetlib.io.NetInput;

import java.io.IOException;

public class ServerEntityPositionRotationPacket extends ServerEntityMovementPacket {
    protected ServerEntityPositionRotationPacket() {
        this.pos = true;
        this.rot = true;
    }

    public ServerEntityPositionRotationPacket(NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.moveX = in.readShort() / 4096D;
        this.moveY = in.readShort() / 4096D;
        this.moveZ = in.readShort() / 4096D;
        this.yaw = in.readByte() * 360 / 256f;
        this.pitch = in.readByte() * 360 / 256f;
        this.onGround = in.readBoolean();
        this.pos = true;
        this.rot = true;
    }

    public ServerEntityPositionRotationPacket(int entityId, double moveX, double moveY, double moveZ, float yaw, float pitch, boolean onGround) {
        super(entityId, onGround);
        this.pos = true;
        this.rot = true;
        this.moveX = moveX;
        this.moveY = moveY;
        this.moveZ = moveZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
