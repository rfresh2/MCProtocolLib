package org.geysermc.mcprotocollib.protocol.data.game.level.particle;

public record TargetColorParticleData(double targetX, double targetY, double targetZ, int color) implements ParticleData {
}
