package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

final class ResolvableProfileSerializerImpl {

    static PlayerHeadObjectContents.Builder deserialize(Tag object) {
        return switch (object) {
            case StringTag name -> ObjectContents.playerHead(name.getValue()).toBuilder();
            case CompoundTag map -> {
                var builder = ObjectContents.playerHead();
                NbtUtil.checkNonNull(map.getString("name"), builder::name);
                NbtUtil.checkNonNull(map.getIntArrayTag("id"), array -> builder.id(NbtUtil.deserializeUUID(array.getValue())));
                NbtUtil.checkNonNull(map.getString("texture"), texture -> builder.texture(Key.key(texture)));

                NbtUtil.checkNonNull(map.getListTag("properties", CompoundTag.class), properties -> properties.stream().map(ResolvableProfileSerializerImpl::deserializeProfileProperty).forEach(builder::profileProperty));
                yield builder;
            }
            default -> throw new IllegalArgumentException("Don't know how to parse resolvable profile: " + object);
        };
    }

    static CompoundTag serialize(PlayerHeadObjectContents profile) {
        var builder = new CompoundTag();
        NbtUtil.checkNonNull(profile.name(), name -> builder.putString("name", name));
        NbtUtil.checkNonNull(profile.id(), uuid -> builder.put("id", new IntArrayTag(NbtUtil.serializeUUID(uuid))));
        NbtUtil.checkNonNull(profile.texture(), texture -> builder.putString("texture", texture.asString()));

        var properties = profile.profileProperties().stream().map(ResolvableProfileSerializerImpl::serializeProfileProperty).toList();
        if (!properties.isEmpty()) {
            builder.put("properties", new ListTag<>(properties));
        }

        return builder;
    }

    private static PlayerHeadObjectContents.ProfileProperty deserializeProfileProperty(CompoundTag map) {
        var name = map.getString("name");
        var value = map.getString("value");
        var signature = map.getString("signature", null);
        return PlayerHeadObjectContents.property(name, value, signature);
    }

    private static CompoundTag serializeProfileProperty(PlayerHeadObjectContents.ProfileProperty property) {
        var builder = new CompoundTag();
        builder.putString("name", property.name());
        builder.putString("value", property.value());
        NbtUtil.checkNonNull(property.signature(), signature -> builder.putString("signature", signature));
        return builder;
    }
}
