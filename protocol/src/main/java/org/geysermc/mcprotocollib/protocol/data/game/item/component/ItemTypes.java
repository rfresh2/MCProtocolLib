package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import com.viaversion.nbt.tag.ListTag;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.Holder;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.ModifierOperation;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.BuiltinSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.CustomSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class ItemTypes {

    public static <T> Filterable<T> readFilterable(ByteBuf buf, Function<ByteBuf, T> reader) {
        T raw = reader.apply(buf);
        T filtered = MinecraftTypes.readNullable(buf, reader);
        return new Filterable<>(raw, filtered);
    }

    public static <T> void writeFilterable(ByteBuf buf, Filterable<T> filterable, BiConsumer<ByteBuf, T> writer) {
        writer.accept(buf, filterable.getRaw());
        MinecraftTypes.writeNullable(buf, filterable.getOptional(), writer);
    }

    public static Unbreakable readUnbreakable(ByteBuf buf) {
        return new Unbreakable(buf.readBoolean());
    }

    public static void writeUnbreakable(ByteBuf buf, Unbreakable unbreakable) {
        buf.writeBoolean(unbreakable.isInTooltip());
    }

    public static ItemEnchantments readItemEnchantments(ByteBuf buf) {
        int enchantmentCount = MinecraftTypes.readVarInt(buf);
        Int2IntMap enchantments = new Int2IntArrayMap(enchantmentCount);
        for (int i = 0; i < enchantmentCount; i++) {
            enchantments.put(MinecraftTypes.readVarInt(buf), MinecraftTypes.readVarInt(buf));
        }

        return new ItemEnchantments(enchantments, buf.readBoolean());
    }

    public static void writeItemEnchantments(ByteBuf buf, ItemEnchantments itemEnchantments) {
        MinecraftTypes.writeVarInt(buf, itemEnchantments.getEnchantments().size());
        for (var entry : itemEnchantments.getEnchantments().int2IntEntrySet()) {
            MinecraftTypes.writeVarInt(buf, entry.getIntKey());
            MinecraftTypes.writeVarInt(buf, entry.getIntValue());
        }

        buf.writeBoolean(itemEnchantments.isShowInTooltip());
    }

    public static AdventureModePredicate readAdventureModePredicate(ByteBuf buf) {
        List<AdventureModePredicate.BlockPredicate> predicates = MinecraftTypes.readList(buf, ItemTypes::readBlockPredicate);
        return new AdventureModePredicate(predicates, buf.readBoolean());
    }

    public static void writeAdventureModePredicate(ByteBuf buf, AdventureModePredicate adventureModePredicate) {
        MinecraftTypes.writeVarInt(buf, adventureModePredicate.getPredicates().size());
        for (int i = 0; i < adventureModePredicate.getPredicates().size(); i++) {
            ItemTypes.writeBlockPredicate(buf, adventureModePredicate.getPredicates().get(i));
        }

        buf.writeBoolean(adventureModePredicate.isShowInTooltip());
    }

    public static AdventureModePredicate.BlockPredicate readBlockPredicate(ByteBuf buf) {
        HolderSet holderSet = MinecraftTypes.readNullable(buf, ItemTypes::readHolderSet);
        List<AdventureModePredicate.PropertyMatcher> propertyMatchers = MinecraftTypes.readNullable(buf, (input) -> {
            int matcherCount = MinecraftTypes.readVarInt(input);
            List<AdventureModePredicate.PropertyMatcher> matchers = new ArrayList<>(matcherCount);
            for (int i = 0; i < matcherCount; i++) {
                String name = MinecraftTypes.readString(input);
                if (input.readBoolean()) {
                    matchers.add(new AdventureModePredicate.PropertyMatcher(name, MinecraftTypes.readString(input), null, null));
                } else {
                    matchers.add(new AdventureModePredicate.PropertyMatcher(name, null, MinecraftTypes.readString(input), MinecraftTypes.readString(input)));
                }
            }
            return matchers;
        });

        return new AdventureModePredicate.BlockPredicate(holderSet, propertyMatchers, MinecraftTypes.readNullable(buf, MinecraftTypes::readMNBT));
    }

    public static void writeBlockPredicate(ByteBuf buf, AdventureModePredicate.BlockPredicate blockPredicate) {
        MinecraftTypes.writeNullable(buf, blockPredicate.getBlocks(), ItemTypes::writeHolderSet);
        MinecraftTypes.writeNullable(buf, blockPredicate.getProperties(), (output, properties) -> {
            buf.writeBoolean(true);
            for (AdventureModePredicate.PropertyMatcher matcher : properties) {
                MinecraftTypes.writeString(buf, matcher.getName());
                if (matcher.getValue() != null) {
                    buf.writeBoolean(true);
                    MinecraftTypes.writeString(buf, matcher.getValue());
                } else {
                    buf.writeBoolean(false);
                    MinecraftTypes.writeString(buf, matcher.getMinValue());
                    MinecraftTypes.writeString(buf, matcher.getMaxValue());
                }
            }
        });

        MinecraftTypes.writeNullable(buf, blockPredicate.getNbt(), MinecraftTypes::writeMNBT);
    }

    public static HolderSet readHolderSet(ByteBuf buf) {
        int length = MinecraftTypes.readVarInt(buf) - 1;
        if (length == -1) {
            return new HolderSet(MinecraftTypes.readResourceLocationString(buf));
        } else {
            int[] holders = new int[length];
            for (int i = 0; i < length; i++) {
                holders[i] = MinecraftTypes.readVarInt(buf);
            }

            return new HolderSet(holders);
        }
    }

    public static void writeHolderSet(ByteBuf buf, HolderSet holderSet) {
        if (holderSet.getLocation() != null) {
            MinecraftTypes.writeVarInt(buf, 0);
            MinecraftTypes.writeResourceLocation(buf, holderSet.getLocation());
        } else {
            assert holderSet.getHolders() != null;
            MinecraftTypes.writeVarInt(buf, holderSet.getHolders().length + 1);
            for (int holder : holderSet.getHolders()) {
                MinecraftTypes.writeVarInt(buf, holder);
            }
        }
    }

    public static ToolData readToolData(ByteBuf buf) {
        List<ToolData.Rule> rules = MinecraftTypes.readList(buf, (input) -> {
            HolderSet holderSet = ItemTypes.readHolderSet(input);

            Float speed = MinecraftTypes.readNullable(input, ByteBuf::readFloat);
            Boolean correctForDrops = MinecraftTypes.readNullable(input, ByteBuf::readBoolean);
            return new ToolData.Rule(holderSet, speed, correctForDrops);
        });

        float defaultMiningSpeed = buf.readFloat();
        int damagePerBlock = MinecraftTypes.readVarInt(buf);
        return new ToolData(rules, defaultMiningSpeed, damagePerBlock);
    }

    public static void writeToolData(ByteBuf buf, ToolData data) {
        MinecraftTypes.writeList(buf, data.getRules(), (output, rule) -> {
            ItemTypes.writeHolderSet(output, rule.getBlocks());
            MinecraftTypes.writeNullable(output, rule.getSpeed(), ByteBuf::writeFloat);
            MinecraftTypes.writeNullable(output, rule.getCorrectForDrops(), ByteBuf::writeBoolean);
        });

        buf.writeFloat(data.getDefaultMiningSpeed());
        MinecraftTypes.writeVarInt(buf, data.getDamagePerBlock());
    }

    public static ItemAttributeModifiers readItemAttributeModifiers(ByteBuf buf) {
        List<ItemAttributeModifiers.Entry> modifiers = MinecraftTypes.readList(buf, (input) -> {
            int attribute = MinecraftTypes.readVarInt(input);

            String id = MinecraftTypes.readResourceLocationString(input);
            double amount = input.readDouble();
            ModifierOperation operation = ModifierOperation.from(MinecraftTypes.readVarInt(input));
            ItemAttributeModifiers.AttributeModifier modifier = new ItemAttributeModifiers.AttributeModifier(id, amount, operation);

            ItemAttributeModifiers.EquipmentSlotGroup slot = ItemAttributeModifiers.EquipmentSlotGroup.from(
                MinecraftTypes.readVarInt(input));
            return new ItemAttributeModifiers.Entry(attribute, modifier, slot);
        });

        return new ItemAttributeModifiers(modifiers, buf.readBoolean());
    }

    public static void writeItemAttributeModifiers(ByteBuf buf, ItemAttributeModifiers modifiers) {
        MinecraftTypes.writeList(buf, modifiers.getModifiers(), (output, entry) -> {
            MinecraftTypes.writeVarInt(output, entry.getAttribute());
            MinecraftTypes.writeResourceLocation(output, entry.getModifier().getId());
            output.writeDouble(entry.getModifier().getAmount());
            MinecraftTypes.writeVarInt(output, entry.getModifier().getOperation().ordinal());
            MinecraftTypes.writeVarInt(output, entry.getSlot().ordinal());
        });

        buf.writeBoolean(modifiers.isShowInTooltip());
    }

    public static DyedItemColor readDyedItemColor(ByteBuf buf) {
        return new DyedItemColor(buf.readInt(), buf.readBoolean());
    }

    public static void writeDyedItemColor(ByteBuf buf, DyedItemColor itemColor) {
        buf.writeInt(itemColor.getRgb());
        buf.writeBoolean(itemColor.isShowInTooltip());
    }

    public static PotionContents readPotionContents(ByteBuf buf) {
        int potionId = buf.readBoolean() ? MinecraftTypes.readVarInt(buf) : -1;
        int customColor = buf.readBoolean() ? buf.readInt() : -1;

        List<MobEffectInstance> customEffects = MinecraftTypes.readList(buf, ItemTypes::readEffectInstance);
        return new PotionContents(potionId, customColor, customEffects);
    }

    public static void writePotionContents(ByteBuf buf, PotionContents contents) {
        if (contents.getPotionId() < 0) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            MinecraftTypes.writeVarInt(buf, contents.getPotionId());
        }

        if (contents.getCustomColor() < 0) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeInt(contents.getCustomColor());
        }

        MinecraftTypes.writeList(buf, contents.getCustomEffects(), ItemTypes::writeEffectInstance);
    }

    public static FoodProperties readFoodProperties(ByteBuf buf) {
        int nutrition = MinecraftTypes.readVarInt(buf);
        float saturationModifier = buf.readFloat();
        boolean canAlwaysEat = buf.readBoolean();
        float eatSeconds = buf.readFloat();
        ItemStack usingConvertsTo = MinecraftTypes.readNullable(buf, MinecraftTypes::readOptionalItemStack);

        List<FoodProperties.PossibleEffect> effects = MinecraftTypes.readList(buf, (input) -> {
            MobEffectInstance effect = ItemTypes.readEffectInstance(input);
            float probability = input.readFloat();
            return new FoodProperties.PossibleEffect(effect, probability);
        });

        return new FoodProperties(nutrition, saturationModifier, canAlwaysEat, eatSeconds, usingConvertsTo, effects);
    }

    public static void writeFoodProperties(ByteBuf buf, FoodProperties properties) {
        MinecraftTypes.writeVarInt(buf, properties.getNutrition());
        buf.writeFloat(properties.getSaturationModifier());
        buf.writeBoolean(properties.isCanAlwaysEat());
        buf.writeFloat(properties.getEatSeconds());
        MinecraftTypes.writeNullable(buf, properties.getUsingConvertsTo(), MinecraftTypes::writeOptionalItemStack);

        MinecraftTypes.writeList(buf, properties.getEffects(), (output, effect) -> {
            ItemTypes.writeEffectInstance(output, effect.getEffect());
            output.writeFloat(effect.getProbability());
        });
    }

    public static MobEffectInstance readEffectInstance(ByteBuf buf) {
        Effect effect = MinecraftTypes.readEffect(buf);
        return new MobEffectInstance(effect, ItemTypes.readEffectDetails(buf));
    }

    public static MobEffectDetails readEffectDetails(ByteBuf buf) {
        int amplifier = MinecraftTypes.readVarInt(buf);
        int duration = MinecraftTypes.readVarInt(buf);
        boolean ambient = buf.readBoolean();
        boolean showParticles = buf.readBoolean();
        boolean showIcon = buf.readBoolean();
        MobEffectDetails hiddenEffect = MinecraftTypes.readNullable(buf, ItemTypes::readEffectDetails);
        return new MobEffectDetails(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
    }

    public static void writeEffectInstance(ByteBuf buf, MobEffectInstance instance) {
        MinecraftTypes.writeEffect(buf, instance.getEffect());
        ItemTypes.writeEffectDetails(buf, instance.getDetails());
    }

    public static void writeEffectDetails(ByteBuf buf, MobEffectDetails details) {
        MinecraftTypes.writeVarInt(buf, details.getAmplifier());
        MinecraftTypes.writeVarInt(buf, details.getDuration());
        buf.writeBoolean(details.isAmbient());
        buf.writeBoolean(details.isShowParticles());
        buf.writeBoolean(details.isShowIcon());
        MinecraftTypes.writeNullable(buf, details.getHiddenEffect(), ItemTypes::writeEffectDetails);
    }

    public static SuspiciousStewEffect readStewEffect(ByteBuf buf) {
        return new SuspiciousStewEffect(MinecraftTypes.readVarInt(buf), MinecraftTypes.readVarInt(buf));
    }

    public static void writeStewEffect(ByteBuf buf, SuspiciousStewEffect effect) {
        MinecraftTypes.writeVarInt(buf, effect.getMobEffectId());
        MinecraftTypes.writeVarInt(buf, effect.getDuration());
    }

    public static WritableBookContent readWritableBookContent(ByteBuf buf) {
        List<Filterable<String>> pages = MinecraftTypes.readList(buf, (input) -> ItemTypes.readFilterable(input, MinecraftTypes::readString));
        return new WritableBookContent(pages);
    }

    public static void writeWritableBookContent(ByteBuf buf, WritableBookContent content) {
        MinecraftTypes.writeList(buf, content.getPages(), (output, page) -> ItemTypes.writeFilterable(output, page, MinecraftTypes::writeString));
    }

    public static WrittenBookContent readWrittenBookContent(ByteBuf buf) {
        Filterable<String> title = ItemTypes.readFilterable(buf, MinecraftTypes::readString);
        String author = MinecraftTypes.readString(buf);
        int generation = MinecraftTypes.readVarInt(buf);

        List<Filterable<Component>> pages = MinecraftTypes.readList(buf, (input) -> ItemTypes.readFilterable(input, MinecraftTypes::readComponent));
        boolean resolved = buf.readBoolean();
        return new WrittenBookContent(title, author, generation, pages, resolved);
    }

    public static void writeWrittenBookContent(ByteBuf buf, WrittenBookContent content) {
        ItemTypes.writeFilterable(buf, content.getTitle(), MinecraftTypes::writeString);
        MinecraftTypes.writeString(buf, content.getAuthor());
        MinecraftTypes.writeVarInt(buf, content.getGeneration());

        MinecraftTypes.writeList(buf, content.getPages(), (output, page) -> ItemTypes.writeFilterable(output, page, MinecraftTypes::writeComponent));

        buf.writeBoolean(content.isResolved());
    }

    public static ArmorTrim readArmorTrim(ByteBuf buf) {
        Holder<ArmorTrim.TrimMaterial> material = MinecraftTypes.readHolder(buf, ItemTypes::readTrimMaterial);
        Holder<ArmorTrim.TrimPattern> pattern = MinecraftTypes.readHolder(buf, ItemTypes::readTrimPattern);
        boolean showInTooltip = buf.readBoolean();
        return new ArmorTrim(material, pattern, showInTooltip);
    }

    public static void writeArmorTrim(ByteBuf buf, ArmorTrim trim) {
        MinecraftTypes.writeHolder(buf, trim.material(), ItemTypes::writeTrimMaterial);
        MinecraftTypes.writeHolder(buf, trim.pattern(), ItemTypes::writeTrimPattern);
        buf.writeBoolean(trim.showInTooltip());
    }

    public static ArmorTrim.TrimMaterial readTrimMaterial(ByteBuf buf) {
        String assetName = MinecraftTypes.readString(buf);
        int ingredientId = MinecraftTypes.readVarInt(buf);
        float itemModelIndex = buf.readFloat();

        Int2ObjectMap<String> overrideArmorMaterials = new Int2ObjectOpenHashMap<>();
        int overrideCount = MinecraftTypes.readVarInt(buf);
        for (int i = 0; i < overrideCount; i++) {
            overrideArmorMaterials.put(MinecraftTypes.readVarInt(buf), MinecraftTypes.readString(buf));
        }

        Component description = MinecraftTypes.readComponent(buf);
        return new ArmorTrim.TrimMaterial(assetName, ingredientId, itemModelIndex, overrideArmorMaterials, description);
    }

    public static void writeTrimMaterial(ByteBuf buf, ArmorTrim.TrimMaterial material) {
        MinecraftTypes.writeString(buf, material.assetName());
        MinecraftTypes.writeVarInt(buf, material.ingredientId());
        buf.writeFloat(material.itemModelIndex());

        MinecraftTypes.writeVarInt(buf, material.overrideArmorMaterials().size());
        for (Int2ObjectMap.Entry<String> entry : material.overrideArmorMaterials().int2ObjectEntrySet()) {
            MinecraftTypes.writeVarInt(buf, entry.getIntKey());
            MinecraftTypes.writeString(buf, entry.getValue());
        }

        MinecraftTypes.writeComponent(buf, material.description());
    }

    public static ArmorTrim.TrimPattern readTrimPattern(ByteBuf buf) {
        String assetId = MinecraftTypes.readResourceLocationString(buf);
        int templateItemId = MinecraftTypes.readVarInt(buf);
        Component description = MinecraftTypes.readComponent(buf);
        boolean decal = buf.readBoolean();
        return new ArmorTrim.TrimPattern(assetId, templateItemId, description, decal);
    }

    public static void writeTrimPattern(ByteBuf buf, ArmorTrim.TrimPattern pattern) {
        MinecraftTypes.writeResourceLocation(buf, pattern.assetId());
        MinecraftTypes.writeVarInt(buf, pattern.templateItemId());
        MinecraftTypes.writeComponent(buf, pattern.description());
        buf.writeBoolean(pattern.decal());
    }

    public static Holder<Instrument> readInstrument(ByteBuf buf) {
        return MinecraftTypes.readHolder(buf, (input) -> {
            Sound soundEvent = MinecraftTypes.readById(input, BuiltinSound::from, MinecraftTypes::readSoundEvent);
            int useDuration = MinecraftTypes.readVarInt(input);
            float range = input.readFloat();
            return new Instrument(soundEvent, useDuration, range);
        });
    }

    public static void writeInstrument(ByteBuf buf, Holder<Instrument> instrumentHolder) {
        MinecraftTypes.writeHolder(buf, instrumentHolder, (output, instrument) -> {
            if (instrument.getSoundEvent() instanceof CustomSound) {
                MinecraftTypes.writeVarInt(buf, 0);
                MinecraftTypes.writeSoundEvent(buf, instrument.getSoundEvent());
            } else {
                MinecraftTypes.writeVarInt(buf, ((BuiltinSound) instrument.getSoundEvent()).ordinal() + 1);
            }

            MinecraftTypes.writeVarInt(buf, instrument.getUseDuration());
            buf.writeFloat(instrument.getRange());
        });
    }

    public static ListTag readRecipes(ByteBuf buf) {
        return MinecraftTypes.readTag(buf, ListTag.class);
    }

    public static void writeRecipes(ByteBuf buf, ListTag recipes) {
        MinecraftTypes.writeTag(buf, recipes);
    }

    public static JukeboxPlayable readJukeboxPlayable(ByteBuf buf) {
        Holder<JukeboxPlayable.JukeboxSong> songHolder = null;
        String songLocation = null;
        if (buf.readBoolean()) {
            songHolder = MinecraftTypes.readHolder(buf, ItemTypes::readJukeboxSong);
        } else {
            songLocation = MinecraftTypes.readResourceLocationString(buf);
        }
        boolean showInTooltip = buf.readBoolean();
        return new JukeboxPlayable(songHolder, songLocation, showInTooltip);
    }

    public static void writeJukeboxPlayable(ByteBuf buf, JukeboxPlayable playable) {
        buf.writeBoolean(playable.songHolder() != null);
        if (playable.songHolder() != null) {
            MinecraftTypes.writeHolder(buf, playable.songHolder(), ItemTypes::writeJukeboxSong);
        } else {
            MinecraftTypes.writeResourceLocation(buf, playable.songLocation());
        }
        buf.writeBoolean(playable.showInTooltip());
    }

    public static JukeboxPlayable.JukeboxSong readJukeboxSong(ByteBuf buf) {
        Sound soundEvent = MinecraftTypes.readById(buf, BuiltinSound::from, MinecraftTypes::readSoundEvent);
        Component description = MinecraftTypes.readComponent(buf);
        float lengthInSeconds = buf.readFloat();
        int comparatorOutput = MinecraftTypes.readVarInt(buf);
        return new JukeboxPlayable.JukeboxSong(soundEvent, description, lengthInSeconds, comparatorOutput);
    }

    public static void writeJukeboxSong(ByteBuf buf, JukeboxPlayable.JukeboxSong song) {
        if (song.soundEvent() instanceof CustomSound) {
            MinecraftTypes.writeVarInt(buf, 0);
            MinecraftTypes.writeSoundEvent(buf, song.soundEvent());
        } else {
            MinecraftTypes.writeVarInt(buf, ((BuiltinSound) song.soundEvent()).ordinal() + 1);
        }
        MinecraftTypes.writeComponent(buf, song.description());
        buf.writeFloat(song.lengthInSeconds());
        MinecraftTypes.writeVarInt(buf, song.comparatorOutput());
    }

    public static LodestoneTracker readLodestoneTarget(ByteBuf buf) {
        return new LodestoneTracker(MinecraftTypes.readNullable(buf, MinecraftTypes::readGlobalPos), buf.readBoolean());
    }

    public static void writeLodestoneTarget(ByteBuf buf, LodestoneTracker target) {
        MinecraftTypes.writeNullable(buf, target.getPos(), MinecraftTypes::writeGlobalPos);
        buf.writeBoolean(target.isTracked());
    }

    public static Fireworks readFireworks(ByteBuf buf) {
        int flightDuration = MinecraftTypes.readVarInt(buf);
        int explosionCount = MinecraftTypes.readVarInt(buf);
        List<Fireworks.FireworkExplosion> explosions = new ArrayList<>(explosionCount);
        for (int i = 0; i < explosionCount; i++) {
            explosions.add(ItemTypes.readFireworkExplosion(buf));
        }

        return new Fireworks(flightDuration, explosions);
    }

    public static void writeFireworks(ByteBuf buf, Fireworks fireworks) {
        MinecraftTypes.writeVarInt(buf, fireworks.getFlightDuration());

        MinecraftTypes.writeVarInt(buf, fireworks.getExplosions().size());
        for (int i = 0; i < fireworks.getExplosions().size(); i++) {
            ItemTypes.writeFireworkExplosion(buf, fireworks.getExplosions().get(i));
        }
    }

    public static Fireworks.FireworkExplosion readFireworkExplosion(ByteBuf buf) {
        int shapeId = MinecraftTypes.readVarInt(buf);

        int[] colors = new int[MinecraftTypes.readVarInt(buf)];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = buf.readInt();
        }

        int[] fadeColors = new int[MinecraftTypes.readVarInt(buf)];
        for (int i = 0; i < fadeColors.length; i++) {
            fadeColors[i] = buf.readInt();
        }

        boolean hasTrail = buf.readBoolean();
        boolean hasTwinkle = buf.readBoolean();
        return new Fireworks.FireworkExplosion(shapeId, colors, fadeColors, hasTrail, hasTwinkle);
    }

    public static void writeFireworkExplosion(ByteBuf buf, Fireworks.FireworkExplosion explosion) {
        MinecraftTypes.writeVarInt(buf, explosion.getShapeId());

        MinecraftTypes.writeVarInt(buf, explosion.getColors().length);
        for (int color : explosion.getColors()) {
            buf.writeInt(color);
        }

        MinecraftTypes.writeVarInt(buf, explosion.getFadeColors().length);
        for (int fadeColor : explosion.getFadeColors()) {
            buf.writeInt(fadeColor);
        }

        buf.writeBoolean(explosion.isHasTrail());
        buf.writeBoolean(explosion.isHasTwinkle());
    }

    public static GameProfile readResolvableProfile(ByteBuf buf) {
        String name = MinecraftTypes.readNullable(buf, MinecraftTypes::readString);
        UUID id = MinecraftTypes.readNullable(buf, MinecraftTypes::readUUID);
        GameProfile profile = new GameProfile(id, name);

        List<GameProfile.Property> properties = MinecraftTypes.readList(buf, MinecraftTypes::readProperty);
        profile.setProperties(properties);

        return profile;
    }

    public static void writeResolvableProfile(ByteBuf buf, GameProfile profile) {
        MinecraftTypes.writeNullable(buf, profile.getName(), MinecraftTypes::writeString);
        MinecraftTypes.writeNullable(buf, profile.getId(), MinecraftTypes::writeUUID);

        MinecraftTypes.writeList(buf, profile.getProperties(), MinecraftTypes::writeProperty);
    }

    public static BannerPatternLayer readBannerPatternLayer(ByteBuf buf) {
        return new BannerPatternLayer(MinecraftTypes.readHolder(buf, ItemTypes::readBannerPattern), MinecraftTypes.readVarInt(buf));
    }

    public static void writeBannerPatternLayer(ByteBuf buf, BannerPatternLayer patternLayer) {
        MinecraftTypes.writeHolder(buf, patternLayer.getPattern(), ItemTypes::writeBannerPattern);
        MinecraftTypes.writeVarInt(buf, patternLayer.getColorId());
    }

    public static BannerPatternLayer.BannerPattern readBannerPattern(ByteBuf buf) {
        return new BannerPatternLayer.BannerPattern(MinecraftTypes.readResourceLocationString(buf), MinecraftTypes.readString(buf));
    }

    public static void writeBannerPattern(ByteBuf buf, BannerPatternLayer.BannerPattern pattern) {
        MinecraftTypes.writeResourceLocation(buf, pattern.getAssetId());
        MinecraftTypes.writeString(buf, pattern.getTranslationKey());
    }

    public static BlockStateProperties readBlockStateProperties(ByteBuf buf) {
        int propertyCount = MinecraftTypes.readVarInt(buf);
        Map<String, String> properties = new HashMap<>(propertyCount);
        for (int i = 0; i < propertyCount; i++) {
            properties.put(MinecraftTypes.readString(buf), MinecraftTypes.readString(buf));
        }

        return new BlockStateProperties(properties);
    }

    public static void writeBlockStateProperties(ByteBuf buf, BlockStateProperties props) {
        MinecraftTypes.writeVarInt(buf, props.getProperties().size());
        for (Map.Entry<String, String> prop : props.getProperties().entrySet()) {
            MinecraftTypes.writeString(buf, prop.getKey());
            MinecraftTypes.writeString(buf, prop.getValue());
        }
    }

    public static BeehiveOccupant readBeehiveOccupant(ByteBuf buf) {
        return new BeehiveOccupant(MinecraftTypes.readMNBT(buf), MinecraftTypes.readVarInt(buf), MinecraftTypes.readVarInt(buf));
    }

    public static void writeBeehiveOccupant(ByteBuf buf, BeehiveOccupant occupant) {
        MinecraftTypes.writeMNBT(buf, occupant.getEntityData());
        MinecraftTypes.writeVarInt(buf, occupant.getTicksInHive());
        MinecraftTypes.writeVarInt(buf, occupant.getMinTicksInHive());
    }
}
