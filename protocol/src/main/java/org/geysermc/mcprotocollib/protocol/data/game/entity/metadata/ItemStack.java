package org.geysermc.mcprotocollib.protocol.data.game.entity.metadata;

import com.github.steveice10.opennbt.mini.MNBT;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.io.MNBTIO;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
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
    @Getter(lazy = true) private final ObjectSet<Enchantment> enchantments = deserializeEnchantments();

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

    private ObjectSet<Enchantment> deserializeEnchantments() {
        final ObjectSet<Enchantment> enchantments = new ObjectOpenHashSet<>(1);
        if (nbt == null) return enchantments;
        try {
            final CompoundTag tag = (CompoundTag) MNBTIO.read(nbt);
            final ListTag enchantmentTagList = tag.getListTag("Enchantments");
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
                enchantments.add(new Enchantment(EnchantmentType.valueOf(id.substring(10).toUpperCase()), level));
            }
        } catch (final Throwable e) {
            // fall through
            // print stacktrace?
        }
        return enchantments;
    }
}
