package org.geysermc.mcprotocollib.protocol.data.game.level.block;

import com.viaversion.nbt.io.MNBTIO;
import com.viaversion.nbt.mini.MNBT;
import com.viaversion.nbt.tag.CompoundTag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

@Data
@AllArgsConstructor
@ToString(exclude = "nbt")
public class BlockEntityInfo {
    /**
     * Relative x coordinate of the block entity within the chunk (0-15)
     */
    private int x;
    private int y;
    /**
     * Relative z coordinate of the block entity within the chunk (0-15)
     */
    private int z;
    private BlockEntityType type;
    private @Nullable MNBT nbt;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private CompoundTag cachedNbtTag = null;

    public @Nullable CompoundTag getNbtTag() {
        if (nbt == null) return null;
        var value = cachedNbtTag;
        if (value == null) {
            synchronized (this) {
                value = cachedNbtTag;
                if (value == null) {
                    // could throw an exception :/
                    value = (CompoundTag) MNBTIO.read(nbt);
                    cachedNbtTag = value;
                }
            }
        }
        return value;
    }

    public void setNbt(@Nullable MNBT nbt) {
        this.nbt = nbt;
        synchronized (this) {
            this.cachedNbtTag = null;
        }
    }
}
