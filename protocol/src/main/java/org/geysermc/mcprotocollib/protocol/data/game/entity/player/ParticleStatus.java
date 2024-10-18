package org.geysermc.mcprotocollib.protocol.data.game.entity.player;

public enum ParticleStatus {
    ALL, DECREASED, MINIMAL;

    private static final ParticleStatus[] VALUES = values();

    public static ParticleStatus from(int id) {
        return VALUES[id];
    }
}
