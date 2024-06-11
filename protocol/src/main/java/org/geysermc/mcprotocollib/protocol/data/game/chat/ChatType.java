package org.geysermc.mcprotocollib.protocol.data.game.chat;

import com.viaversion.nbt.mini.MNBT;

import java.util.List;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
    public record ChatTypeDecorationImpl(String translationKey,
                                         List<ChatTypeDecoration.Parameter> parameters,
                                         MNBT style) implements ChatTypeDecoration {
    }
}
