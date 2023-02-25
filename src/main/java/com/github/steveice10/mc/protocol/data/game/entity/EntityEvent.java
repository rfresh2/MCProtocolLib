package com.github.steveice10.mc.protocol.data.game.entity;

public enum EntityEvent {
    TIPPED_ARROW_EMIT_PARTICLES,
    RABBIT_JUMP_OR_MINECART_SPAWNER_DELAY_RESET,
    LIVING_HURT,
    LIVING_DEATH,
    ATTACK,
    STOP_ATTACK, // Unused
    TAMEABLE_TAMING_FAILED,
    TAMEABLE_TAMING_SUCCEEDED,
    WOLF_SHAKE_WATER,
    PLAYER_FINISH_USING_ITEM,
    SHEEP_GRAZE_OR_TNT_CART_EXPLODE,
    IRON_GOLEM_HOLD_POPPY,
    VILLAGER_MATE,
    VILLAGER_ANGRY,
    VILLAGER_HAPPY,
    WITCH_EMIT_PARTICLES,
    ZOMBIE_VILLAGER_CURE,
    FIREWORK_EXPLODE,
    ANIMAL_EMIT_HEARTS,
    SQUID_RESET_ROTATION,
    MOB_EMIT_SMOKE,
    GUARDIAN_MAKE_SOUND,
    PLAYER_ENABLE_REDUCED_DEBUG,
    PLAYER_DISABLE_REDUCED_DEBUG,
    PLAYER_OP_PERMISSION_LEVEL_0,
    PLAYER_OP_PERMISSION_LEVEL_1,
    PLAYER_OP_PERMISSION_LEVEL_2,
    PLAYER_OP_PERMISSION_LEVEL_3,
    PLAYER_OP_PERMISSION_LEVEL_4,
    LIVING_SHIELD_BLOCK,
    LIVING_SHIELD_BREAK,
    FISHING_HOOK_PULL_PLAYER,
    ARMOR_STAND_HIT,
    LIVING_HURT_THORNS,
    IRON_GOLEM_EMPTY_HAND,
    TOTEM_OF_UNDYING_MAKE_SOUND,
    LIVING_DROWN,
    LIVING_BURN,
    DOLPHIN_HAPPY,
    RAVAGER_STUNNED,
    OCELOT_TAMING_FAILED,
    OCELOT_TAMING_SUCCEEDED,
    VILLAGER_SWEAT, // water particle emitted randomly while raid is active
    PLAYER_EMIT_CLOUD, // sent to player whose Bad Omen effect is activated
    LIVING_HURT_SWEET_BERRY_BUSH,
    FOX_EATING,
    LIVING_TELEPORT,
    LIVING_EQUIPMENT_BREAK_MAIN_HAND,
    LIVING_EQUIPMENT_BREAK_OFF_HAND,
    LIVING_EQUIPMENT_BREAK_HEAD,
    LIVING_EQUIPMENT_BREAK_CHEST,
    LIVING_EQUIPMENT_BREAK_LEGS,
    LIVING_EQUIPMENT_BREAK_FEET,
    HONEY_BLOCK_SLIDE,
    HONEY_BLOCK_LAND,
    PLAYER_SWAP_SAME_ITEM,
    WOLF_SHAKE_WATER_STOP,
    LIVING_FREEZE,
    GOAT_LOWERING_HEAD,
    GOAT_STOP_LOWERING_HEAD,
    MAKE_POOF_PARTICLES,
    WARDEN_RECEIVE_SIGNAL,
    WARDEN_SONIC_BOOM,
    SNIFFER_MAKE_SOUND;

    private static final EntityEvent[] VALUES = values();

    public static EntityEvent from(int id) {
        return VALUES[id];
    }
}
