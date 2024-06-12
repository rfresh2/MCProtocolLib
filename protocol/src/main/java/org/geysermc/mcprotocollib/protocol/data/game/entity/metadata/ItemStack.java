package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata;

import com.viaversion.nbt.io.MNBTIO;
import com.viaversion.nbt.mini.MNBT;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Data
@EqualsAndHashCode(exclude = "enchantments")
public class ItemStack {
    private final int id;
    private int amount;
    private final @Nullable MNBT nbt;
    // enchantment type -> int level
    @Getter(lazy = true) private final Object2IntMap<EnchantmentType> enchantments = deserializeEnchantments();
    // nullable
    @Getter(lazy = true) private final CompoundTag compoundTag = deserializeNbt();

    private CompoundTag deserializeNbt() {
        if (nbt == null) return null;
        try {
            return (CompoundTag) MNBTIO.read(nbt);
        } catch (final Exception e) {
            return null;
        }
    }

    public ItemStack(int id, int amount, @Nullable MNBT nbt) {
        this.id = id;
        this.amount = amount;
        this.nbt = nbt;
    }

    public ItemStack(int id) {
        this(id, 1);
    }

    public ItemStack(int id, int amount) {
        this(id, amount, null);
    }

    private Object2IntMap<EnchantmentType> deserializeEnchantments() {
        final Object2IntMap<EnchantmentType> enchantments = new Object2IntOpenHashMap<>(1);
        if (nbt == null) return enchantments;
        try {
            final CompoundTag tag = getCompoundTag();
            final ListTag<CompoundTag> enchantmentTagList = tag.getListTag("Enchantments", CompoundTag.class);
            if (enchantmentTagList == null) return enchantments;
            for (int i = 0; i < enchantmentTagList.size(); i++) {
                final CompoundTag enchantmentTag = enchantmentTagList.get(i);
                final StringTag idTag = enchantmentTag.getStringTag("id");
                if (idTag == null) continue;
                String id = idTag.getValue();
                if (!id.startsWith("minecraft:")) continue;
                NumberTag levelTag = enchantmentTag.getNumberTag("lvl");
                if (levelTag == null) continue;
                final int level = levelTag.asInt();
                enchantments.put(EnchantmentType.valueOf(id.substring(10).toUpperCase()), level);
            }
        } catch (final Throwable e) {
            // fall through
            // print stacktrace?
        }
        return enchantments;
    }
}
