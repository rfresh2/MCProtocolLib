package org.geysermc.mcprotocollib.protocol.data.game.entity.player;

public enum PlayerAction {
    START_DESTROY_BLOCK,
    ABORT_DESTROY_BLOCK,
    STOP_DESTROY_BLOCK,
    DROP_ALL_ITEMS,
    DROP_ITEM,
    RELEASE_USE_ITEM,
    SWAP_ITEM_WITH_OFFHAND,
    STAB;

    private static final PlayerAction[] VALUES = values();

    public static PlayerAction from(int id) {
        return VALUES[id];
    }
}
