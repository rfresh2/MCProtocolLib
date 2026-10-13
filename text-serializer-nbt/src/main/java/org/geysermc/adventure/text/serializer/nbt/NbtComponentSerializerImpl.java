package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.NBTComponentBuilder;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Optional;
import java.util.function.UnaryOperator;

final class NbtComponentSerializerImpl implements NbtComponentSerializer {
    static final NbtComponentSerializerImpl INSTANCE = new NbtComponentSerializerImpl();

    @Override
    public Component deserialize(Tag input) {
        return switch (input) {
            case StringTag simpleText -> Component.text(simpleText.getValue());
            // Vanilla takes the first element in the list and appends the rest of them to that
            // Vanilla also refuses to accept empty lists
            // See https://mcsrc.dev/2/26.2/net/minecraft/network/chat/ComponentSerialization#L108
            case ListTag<?> list -> HeterogeneousNbtList.tryUnwrap(list).stream()
                .map(this::deserialize)
                .map(Component::toBuilder)
                .reduce(ComponentBuilder::append)
                .map(ComponentBuilder::build)
                .orElseThrow(() -> new IllegalArgumentException("List of text components must have at least one element"));
            case CompoundTag map -> deserializeFuzzyComponent(map)
                .append(HeterogeneousNbtList.getList(map, "extra").stream().map(this::deserialize).toList())
                .style(StyleSerializerImpl.deserialize(map, this))
                .build();
            default -> throw new IllegalArgumentException("Don't know how to parse component: " + input);
        };
    }

    @Override
    public Tag serialize(Component component) {
        if (component instanceof TextComponent text && component.children().isEmpty() && !component.hasStyling()) {
            return new StringTag(text.content());
        }

        // switch case will intentionally break compile with new component types
        var builder = switch (component) {
            case TextComponent text -> {
                var map = new CompoundTag();
                map.putString("text", text.content());
                yield map;
            }
            case TranslatableComponent translatable -> {
                var map = new CompoundTag();
                map.putString("translate", translatable.key());
                NbtUtil.checkNonNull(translatable.fallback(), fallback -> map.putString("fallback", fallback));

                var arguments = translatable.arguments().stream().map(this::serializeTranslationArgument).collect(HeterogeneousNbtList.collector());
                if (!arguments.isEmpty()) {
                    map.put("with", arguments);
                }
                yield map;
            }
            case KeybindComponent keybind -> {
                var map = new CompoundTag();
                map.putString("keybind", keybind.keybind());
                yield map;
            }
            case ScoreComponent score -> {
                var contents = new CompoundTag();
                contents.putString("name", score.name());
                contents.putString("objective", score.objective());
                var map = new CompoundTag();
                map.put("score", contents);
                yield map;
            }
            case SelectorComponent selector -> {
                var map = new CompoundTag();
                map.putString("selector", selector.pattern());
                NbtUtil.checkNonNull(selector.separator(), separator -> map.put("separator", serialize(separator)));
                yield map;
            }
            case NBTComponent<?> nbtComponent -> serializeNbtContentsComponent(nbtComponent);
            case ObjectComponent objectComponent -> serializeObjectComponent(objectComponent);
        };

        if (!component.children().isEmpty()) {
            builder.put("extra", component.children().stream().map(this::serialize).collect(HeterogeneousNbtList.collector()));
        }
        StyleSerializerImpl.serialize(builder, component.style(), this);

        return builder;
    }

    // When adding support for new components, make sure to follow the same parsing order as here:
    // https://mcsrc.dev/2/26.2/net/minecraft/network/chat/ComponentSerialization#L120-128
    private ComponentBuilder<?, ?> deserializeFuzzyComponent(CompoundTag map) {
        var text = map.getString("text", null);
        if (text != null) {
            return Component.text().content(text);
        }
        var translate = map.getString("translate", null);
        if (translate != null) {
            var fallback = map.getString("fallback", null);
            var translationArguments = HeterogeneousNbtList.getList(map, "with").stream().map(this::deserializeTranslationArgument).toList();
            return Component.translatable()
                .key(translate)
                .fallback(fallback)
                .arguments(translationArguments);
        }
        var keybind = map.getString("keybind", null);
        if (keybind != null) {
            return Component.keybind().keybind(keybind);
        }
        var score = map.getCompoundTag("score");
        if (score != null) {
            return Component.score()
                .name(score.getString("name", null))
                .objective(score.getString("objective", null));
        }
        var selector = map.getString("selector", null);
        if (selector != null) {
            var separator = deserializeOrNull(map.get("separator"));
            return Component.selector()
                .pattern(selector)
                .separator(separator);
        }
        return deserializeFuzzyNbtContentsComponent(map)
            .or(() -> deserializeFuzzyObjectComponent(map))
            .orElseThrow(() -> new IllegalArgumentException("Don't know how to parse component: " + map));
    }

    @VisibleForTesting
    TranslationArgument deserializeTranslationArgument(Tag object) {
        return switch (object) {
            case ByteTag bool -> TranslationArgument.bool(bool.asBoolean());
            case NumberTag number -> TranslationArgument.numeric(number.getValue());
            default -> TranslationArgument.component(deserialize(object));
        };
    }

    @VisibleForTesting
    Tag serializeTranslationArgument(TranslationArgument argument) {
        if (argument.value() instanceof Component component) {
            return serialize(component);
        }
        return switch (argument.value()) {
            case Boolean bool -> new ByteTag((byte) (bool ? 1 : 0));
            case Number number -> NbtUtil.serializeNumber(number);
            default -> throw new IllegalArgumentException("Unsupported translation argument: " + argument);
        };
    }

    @VisibleForTesting
    Optional<ComponentBuilder<?, ?>> deserializeFuzzyNbtContentsComponent(CompoundTag map) {
        var nbtPath = map.getString("nbt", null);
        if (nbtPath == null) {
            return Optional.empty();
        }

        var interpret = map.getBoolean("interpret", false);
        var plain = map.getBoolean("plain", false);
        var separator = deserializeOrNull(map.get("separator"));

        UnaryOperator<NBTComponentBuilder<?, ?>> baseBuilder = builder -> builder
            .nbtPath(nbtPath)
            .interpret(interpret)
            .plain(plain)
            .separator(separator);

        var entity = map.getString("entity", null);
        if (entity != null) {
            return Optional.of(baseBuilder.apply(Component.entityNBT().selector(entity)));
        }
        var block = map.getString("block", null);
        if (block != null) {
            return Optional.of(baseBuilder.apply(Component.blockNBT().pos(BlockNBTComponent.Pos.fromString(block))));
        }
        var storage = map.getString("storage", null);
        if (storage != null) {
            return Optional.of(baseBuilder.apply(Component.storageNBT().storage(Key.key(storage))));
        }
        throw new IllegalArgumentException("Don't know how to parse NBT component: " + map);
    }

    @VisibleForTesting
    CompoundTag serializeNbtContentsComponent(NBTComponent<?> component) {
        var builder = new CompoundTag();

        builder.putString("nbt", component.nbtPath());
        NbtUtil.putIfTrue(builder, "interpret", component.interpret());
        NbtUtil.putIfTrue(builder, "plain", component.plain());
        NbtUtil.checkNonNull(component.separator(), separator -> builder.put("separator", serialize(separator)));

        // switch case will intentionally break compile with new component types
        switch (component) {
            case EntityNBTComponent entity -> builder.putString("entity", entity.selector());
            case BlockNBTComponent block -> builder.putString("block", block.pos().asString());
            case StorageNBTComponent storage -> builder.putString("storage", storage.storage().asString());
        }
        return builder;
    }

    @VisibleForTesting
    Optional<ComponentBuilder<?, ?>> deserializeFuzzyObjectComponent(CompoundTag map) {
        var fallback = deserializeOrNull(map.get("fallback"));
        var sprite = map.getString("sprite", null);
        if (sprite != null) {
            var atlas = map.getString("atlas", null);
            return Optional.of(Component.object()
                .contents(ObjectContents.sprite(atlas == null ? SpriteObjectContents.DEFAULT_ATLAS : Key.key(atlas), Key.key(sprite)))
                .fallback(fallback));
        }
        var player = map.get("player");
        if (player != null) {
            var contents = ResolvableProfileSerializerImpl.deserialize(player)
                .hat(map.getBoolean("hat", true));
            return Optional.of(Component.object()
                .contents(contents.build())
                .fallback(fallback));
        }
        return Optional.empty();
    }

    @VisibleForTesting
    CompoundTag serializeObjectComponent(ObjectComponent component) {
        var builder = new CompoundTag();
        NbtUtil.checkNonNull(component.fallback(), fallback -> builder.put("fallback", serialize(fallback)));

        // switch case will intentionally break compile with new component types
        switch (component.contents()) {
            case SpriteObjectContents sprite -> {
                if (!sprite.atlas().equals(SpriteObjectContents.DEFAULT_ATLAS)) {
                    builder.putString("atlas", sprite.atlas().asString());
                }
                builder.putString("sprite", sprite.sprite().asString());
            }
            case PlayerHeadObjectContents playerHead -> {
                builder.put("player", ResolvableProfileSerializerImpl.serialize(playerHead));
                NbtUtil.putIfFalse(builder, "hat", playerHead.hat());
            }
        }
        return builder;
    }
}
