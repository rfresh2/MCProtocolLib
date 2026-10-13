package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

final class StyleSerializerImpl {

    static Style deserialize(CompoundTag map, NbtComponentSerializer componentSerializer) {
        return Style.style(builder -> {
            NbtUtil.checkNonNull(map.getString("color"), color -> {
                var parsed = TextColor.fromHexString(color);
                if (parsed == null) {
                    parsed = NamedTextColor.NAMES.valueOrThrow(color);
                }
                builder.color(parsed);
            });
            var shadowColor = map.get("shadow_color");
            if (shadowColor != null) {
                builder.shadowColor(NbtUtil.deserializeARGB(shadowColor));
            }

            NbtUtil.checkNonNull(map.getNumberTag("bold"), value -> builder.decoration(TextDecoration.BOLD, value.asBoolean()));
            NbtUtil.checkNonNull(map.getNumberTag("italic"), value -> builder.decoration(TextDecoration.ITALIC, value.asBoolean()));
            NbtUtil.checkNonNull(map.getNumberTag("underlined"), value -> builder.decoration(TextDecoration.UNDERLINED, value.asBoolean()));
            NbtUtil.checkNonNull(map.getNumberTag("strikethrough"), value -> builder.decoration(TextDecoration.STRIKETHROUGH, value.asBoolean()));
            NbtUtil.checkNonNull(map.getNumberTag("obfuscated"), value -> builder.decoration(TextDecoration.OBFUSCATED, value.asBoolean()));

            NbtUtil.checkNonNull(map.getCompoundTag("click_event"), clickEvent -> builder.clickEvent(ClickEventSerializerImpl.deserialize(clickEvent)));
            NbtUtil.checkNonNull(map.getCompoundTag("hover_event"), hoverEvent -> builder.hoverEvent(HoverEventSerializerImpl.deserialize(hoverEvent, componentSerializer)));

            NbtUtil.checkNonNull(map.getString("insertion"), builder::insertion);
            NbtUtil.checkNonNull(map.getString("font"), font -> builder.font(Key.key(font)));
        });
    }

    static void serialize(CompoundTag builder, Style style, NbtComponentSerializer componentSerializer) {
        NbtUtil.checkNonNull(style.color(), color -> {
            if (color instanceof NamedTextColor named) {
                builder.putString("color", named.name());
            } else {
                builder.putString("color", color.asHexString());
            }
        });
        NbtUtil.checkNonNull(style.shadowColor(), color -> builder.putInt("shadow_color", color.value()));

        style.decorations().forEach((decoration, state) -> {
            if (state != TextDecoration.State.NOT_SET) {
                builder.putBoolean(decoration.toString(), state == TextDecoration.State.TRUE);
            }
        });

        NbtUtil.checkNonNull(style.clickEvent(), clickEvent -> builder.put("click_event", ClickEventSerializerImpl.serialize(clickEvent)));
        NbtUtil.checkNonNull(style.hoverEvent(), hoverEvent -> builder.put("hover_event", HoverEventSerializerImpl.serialize(hoverEvent, componentSerializer)));

        NbtUtil.checkNonNull(style.insertion(), insertion -> builder.putString("insertion", insertion));
        NbtUtil.checkNonNull(style.font(), font -> builder.putString("font", font.asString()));
    }
}
