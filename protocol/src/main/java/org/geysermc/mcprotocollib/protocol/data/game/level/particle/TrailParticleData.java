package org.geysermc.mcprotocollib.protocol.data.game.level.particle;

public record TrailParticleData(double targetX, double targetY, double targetZ, int color, int duration) implements ParticleData {
}
