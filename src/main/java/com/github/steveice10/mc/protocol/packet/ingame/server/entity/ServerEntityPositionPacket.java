package com.github.steveice10.mc.protocol.packet.ingame.server.entity;

import com.github.steveice10.packetlib.io.NetInput;

import java.io.IOException;

public class ServerEntityPositionPacket extends ServerEntityMovementPacket {
    protected ServerEntityPositionPacket() {
        this.pos = true;
    }

    public ServerEntityPositionPacket(NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.moveX = in.readShort() / 4096D;
        this.moveY = in.readShort() / 4096D;
        this.moveZ = in.readShort() / 4096D;
        this.onGround = in.readBoolean();
        this.pos = true;
    }

    public ServerEntityPositionPacket(int entityId, double moveX, double moveY, double moveZ, boolean onGround) {
        super(entityId, onGround);
        this.pos = true;
        this.moveX = moveX;
        this.moveY = moveY;
        this.moveZ = moveZ;
    }
}
