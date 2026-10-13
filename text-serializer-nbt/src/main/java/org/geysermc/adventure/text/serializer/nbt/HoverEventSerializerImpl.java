package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class HoverEventSerializerImpl {
    @VisibleForTesting
    static final List<HoverEvent.Action<?>> SUPPORTED_EVENTS = List.of(HoverEvent.Action.SHOW_TEXT, HoverEvent.Action.SHOW_ITEM,
        HoverEvent.Action.SHOW_ENTITY, HoverEvent.Action.SHOW_ACHIEVEMENT); // We throw on show_achievement, but that's intended

    static HoverEvent<?> deserialize(CompoundTag map, NbtComponentSerializer componentSerializer) {
        var action = HoverEvent.Action.NAMES.valueOrThrow(map.getString("action"));

        if (action == HoverEvent.Action.SHOW_TEXT) {
            return HoverEvent.showText(componentSerializer.deserialize(map.get("value")));
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            var id = Key.key(map.getString("id"));
            var count = map.getInt("count", 1);
            var components = map.getCompoundTag("components");
            return HoverEvent.showItem(id, count, deserializeDataComponents(components));
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            var id = Key.key(map.getString("id"));
            var uuid = NbtUtil.deserializeLenientUUID(map.get("uuid"));
            var name = map.get("name");
            return HoverEvent.showEntity(id, uuid, componentSerializer.deserializeOrNull(name));
        } else {
            throw new IllegalArgumentException("Don't know how to parse hover event action: " + action.name());
        }
    }

    static CompoundTag serialize(HoverEvent<?> event, NbtComponentSerializer componentSerializer) {
        var builder = new CompoundTag();
        builder.putString("action", event.action().name());

        var action = event.action();
        var value = event.value();

        if (action == HoverEvent.Action.SHOW_TEXT) {
            builder.put("value", componentSerializer.serialize((Component) value));
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            var item = (HoverEvent.ShowItem) value;
            builder.putString("id", item.item().asString());
            if (item.count() != 1) {
                builder.putInt("count", item.count());
            }
            if (!item.dataComponents().isEmpty()) {
                builder.put("components", serializeDataComponents(item.dataComponents()));
            }
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            var entity = (HoverEvent.ShowEntity) value;
            builder.putString("id", entity.type().asString());
            builder.put("uuid", new IntArrayTag(NbtUtil.serializeUUID(entity.id())));

            if (entity.name() != null) {
                builder.put("name", componentSerializer.serialize(entity.name()));
            }
        } else {
            throw new IllegalArgumentException("Don't know how to encode hover event action: " + action.name());
        }

        return builder;
    }

    private static Map<Key, ? extends DataComponentValue> deserializeDataComponents(@Nullable CompoundTag components) {
        if (components == null) {
            return Map.of();
        }

        var deserialized = new HashMap<Key, DataComponentValue>();
        components.getValue().forEach((string, tag) -> {
            // '!' indicates removed component
            if (string.startsWith("!")) {
                deserialized.put(Key.key(string.substring(1)), DataComponentValue.removed());
            } else {
                deserialized.put(Key.key(string), new NbtDataComponentValue(tag));
            }
        });
        return Collections.unmodifiableMap(deserialized);
    }

    @VisibleForTesting
    static CompoundTag serializeDataComponents(Map<Key, ? extends DataComponentValue> components) {
        if (components.isEmpty()) {
            return new CompoundTag();
        }

        var serialized = new CompoundTag();
        components.forEach((key, component) -> {
            if (component instanceof DataComponentValue.Removed) {
                // Removed components are prefixed with a '!', and always represented as an empty map
                serialized.put("!" + key.asString(), new CompoundTag());
            } else if (component instanceof NbtDataComponentValue(Tag tag)) {
                serialized.put(key.asString(), tag);
            } else {
                throw new IllegalArgumentException("Don't know how to serialise component of type: " + component.getClass());
            }
        });
        return serialized;
    }
}
