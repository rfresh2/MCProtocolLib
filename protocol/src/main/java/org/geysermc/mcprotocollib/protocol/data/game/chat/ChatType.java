package org.geysermc.mcprotocollib.protocol.data.game.chat;

import com.viaversion.nbt.mini.MNBT;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
    public record ChatTypeDecoration(String translationKey, int[] parameters, MNBT style) {
    }
}
