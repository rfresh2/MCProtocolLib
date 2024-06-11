package org.geysermc.mcprotocollib.protocol.data.game.chat;

import com.viaversion.nbt.mini.MNBT;

import java.util.List;

// Here for implementation if one wants to cache the Style tag while we don't have DFU Codecs.
public interface ChatTypeDecoration {
    String translationKey();

    List<Parameter> parameters();

    MNBT style();

    enum Parameter {
        CONTENT,
        SENDER,
        TARGET;

        public static final Parameter[] VALUES = values();
    }
}
