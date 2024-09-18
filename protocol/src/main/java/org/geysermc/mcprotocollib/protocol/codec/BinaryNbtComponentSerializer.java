package org.geysermc.mcprotocollib.protocol.codec;

import com.google.gson.internal.LazilyParsedNumber;
import com.viaversion.nbt.mini.MNBT;
import com.viaversion.nbt.mini.MNBTWriter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class BinaryNbtComponentSerializer {
    private static final Logger LOGGER = getLogger("Proxy");

    public static MNBT serializeToMNBT(Component component) {
        try (MNBTWriter writer = new MNBTWriter()) {
            serialize(writer, component);
            return writer.toMNBT();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void serializeMNBTToBuffer(Component component, DataOutputStream buf) {
        try (MNBTWriter writer = new MNBTWriter(buf)) {
            serialize(writer, component);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void serialize(MNBTWriter writer, Component component) {
        writer.writeStartTag();
        serializeComponent(writer, component);
        writer.writeEndTag();
    }

    public static void serializeComponent(MNBTWriter writer, Component component) {
        if (component instanceof TextComponent textComponent) {
            serializeTextComponent(writer, textComponent);
        } else if (component instanceof TranslatableComponent translatableComponent) {
            serializeTranslatableComponent(writer, translatableComponent);
        } else if (component instanceof KeybindComponent keybindComponent) {
            serializeKeybindComponent(writer, keybindComponent);
        } else if (component instanceof ScoreComponent scoreComponent) {
            serializeScoreComponent(writer, scoreComponent);
        } else if (component instanceof SelectorComponent selectorComponent) {
            serializeSelectorComponent(writer, selectorComponent);
        } else {
            LOGGER.error("BinaryNbtComponentSerializer: Unknown component type: {}", component.getClass().getName());
        }
        if (!component.style().isEmpty()) {
            serializeStyle(writer, component.style());
        }
        if (!component.children().isEmpty()) {
            writer.writeListTag("extra", 10, component.children().size());
            List<Component> children = component.children();
            for (int i = 0; i < children.size(); i++) {
                final Component child = children.get(i);
                serializeComponent(writer, child);
                writer.writeEndTag();
            }
        }
    }

    public static void serializeTextComponent(MNBTWriter writer, TextComponent component) {
        writer.writeStringTag("type", "text");
        writer.writeStringTag("text", component.content());
    }

    public static void serializeTranslatableComponent(MNBTWriter writer, TranslatableComponent component) {
        writer.writeStringTag("type", "translatable");
        writer.writeStringTag("translate", component.key());
        String fallback = component.fallback();
        if (fallback != null) {
            writer.writeStringTag("fallback", fallback);
        }
        List<TranslationArgument> arguments = component.arguments();
        if (!arguments.isEmpty()) {
            if (arguments.size() == 1) {
                var arg = arguments.get(0);
                var argValue = arg.value();
                if (argValue instanceof Component) {
                    writer.writeListTag("with", 10, 1);
                    serializeComponent(writer, (Component) argValue);
                    writer.writeEndTag();
                } else {
                    if (argValue instanceof LazilyParsedNumber lazy) {
                        if (lazy.toString().contains(".")) {
                            writer.writeListTag("with", 6, 1);
                            writer.writeDoubleTag(lazy.doubleValue());
                        }
                        else {
                            var longVal = lazy.longValue();
                            if (longVal <= Integer.MAX_VALUE && longVal >= Integer.MIN_VALUE) {
                                writer.writeListTag("with", 3, 1);
                                writer.writeIntTag(lazy.intValue());
                            } else {
                                writer.writeListTag("with", 4, 1);
                                writer.writeLongTag(longVal);
                            }
                        }
                    } else if (argValue instanceof String) {
                        writer.writeListTag("with", 8, 1);
                        writer.writeStringTag((String) argValue);
                    } else if (argValue instanceof Boolean) {
                        writer.writeListTag("with", 1, 1);
                        writer.writeByteTag((Boolean) argValue ? (byte) 1 : (byte) 0);
                    } else if (argValue instanceof Short) {
                        writer.writeListTag("with", 2, 1);
                        writer.writeShortTag((Short) argValue);
                    } else if (argValue instanceof Integer) {
                        writer.writeListTag("with", 3, 1);
                        writer.writeIntTag((Integer) argValue);
                    } else if (argValue instanceof Long) {
                        writer.writeListTag("with", 4, 1);
                        writer.writeLongTag((Long) argValue);
                    } else if (argValue instanceof Float) {
                        writer.writeListTag("with", 5, 1);
                        writer.writeFloatTag((Float) argValue);
                    } else if (argValue instanceof Double) {
                        writer.writeListTag("with", 6, 1);
                        writer.writeDoubleTag((Double) argValue);
                    }
                }
            } else {
                writer.writeListTag("with", 10, arguments.size());
                for (int i = 0; i < arguments.size(); i++) {
                    var arg = arguments.get(i);
                    var argValue = arg.value();
                    if (argValue instanceof Component) {
                        serializeComponent(writer, (Component) argValue);
                    } else {
                        if (argValue instanceof final LazilyParsedNumber lazy) {
                            if (lazy.toString().contains(".")) writer.writeFloatTag(lazy.floatValue());
                            else {
                                var longVal = lazy.longValue();
                                if (longVal <= Integer.MAX_VALUE && longVal >= Integer.MIN_VALUE)
                                    writer.writeIntTag("", lazy.intValue());
                                else
                                    writer.writeLongTag("", longVal);
                            }
                        }
                        if (argValue instanceof Boolean) writer.writeByteTag("", (Boolean) argValue ? (byte) 1 : (byte) 0);
                        else if (argValue instanceof String) writer.writeStringTag("", (String) argValue);
                        else if (argValue instanceof Integer) writer.writeIntTag("", (Integer) argValue);
                        else if (argValue instanceof Short) writer.writeShortTag("", (Short) argValue);
                        else if (argValue instanceof Long) writer.writeLongTag("", (Long) argValue);
                        else if (argValue instanceof Float) writer.writeFloatTag("", (Float) argValue);
                        else if (argValue instanceof Double) writer.writeDoubleTag("", (Double) argValue);
                    }
                    writer.writeEndTag();
                }
            }
        }
    }

    public static void serializeKeybindComponent(MNBTWriter writer, KeybindComponent component) {
        writer.writeStringTag("type", "keybind");
        writer.writeStringTag("keybind", component.keybind());
    }

    public static void serializeStyle(MNBTWriter writer, Style style) {
        var color = style.color();
        if (color != null) {
            writer.writeStringTag("color", color.toString());
        }
        for (var entry : style.decorations().entrySet()) {
            TextDecoration decoration = entry.getKey();
            TextDecoration.State state = entry.getValue();
            if (state != TextDecoration.State.NOT_SET) {
                var decorationName = switch (decoration) {
                    case OBFUSCATED -> "obfuscated";
                    case BOLD -> "bold";
                    case STRIKETHROUGH -> "strikethrough";
                    case UNDERLINED -> "underlined";
                    case ITALIC -> "italic";
                };
                writer.writeByteTag(decorationName, (byte) (state == TextDecoration.State.TRUE ? 1 : 0));
            }
        }
        var font = style.font();
        if (font != null) {
            writer.writeStringTag("font", font.asString());
        }
        var insertion = style.insertion();
        if (insertion != null) {
            writer.writeStringTag("insertion", insertion);
        }
        var clickEvent = style.clickEvent();
        if (clickEvent != null) {
            writer.writeCompoundTag("clickEvent");
            writer.writeStringTag("action", clickEvent.action().toString());
            writer.writeStringTag("value", clickEvent.value());
            writer.writeEndTag();
        }
        var hoverEvent = style.hoverEvent();
        if (hoverEvent != null) {
            serializeHoverEvent(writer, hoverEvent);
        }
    }

    public static void serializeHoverEvent(MNBTWriter writer, HoverEvent<?> hoverEvent) {
        var value = hoverEvent.value();
        HoverEvent.Action<?> action = hoverEvent.action();
        if (action != HoverEvent.Action.SHOW_TEXT && action != HoverEvent.Action.SHOW_ITEM && action != HoverEvent.Action.SHOW_ENTITY) {
            return;
        }
        writer.writeCompoundTag("hoverEvent");
        writer.writeStringTag("action", action.toString());
        writer.writeCompoundTag("contents");
        if (action == HoverEvent.Action.SHOW_TEXT) {
            serializeComponent(writer, (Component) value);
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            HoverEvent.ShowItem item = (HoverEvent.ShowItem) value;
            writer.writeStringTag("id", item.item().asString());
            writer.writeIntTag("count", item.count());
            @SuppressWarnings("deprecation")
            BinaryTagHolder nbt = item.nbt(); // replaced with data components on 1.20.5+
            if (nbt != null) {
                writer.writeStringTag("tag", nbt.string());
            }
            Map<Key, DataComponentValue> dataComponents = item.dataComponents();
            if (!dataComponents.isEmpty()) {
                for (var entry : dataComponents.entrySet()) {
                    DataComponentValue dataComponentValue = entry.getValue();
                    if (dataComponentValue instanceof DataComponentValue.TagSerializable tagSerializable) {
                        BinaryTagHolder binaryTag = tagSerializable.asBinaryTag();
                        // todo: not confident this will work
                        //  need to convert this holder into actual nbt somehow
                        writer.writeStringTag(entry.getKey().asString(), binaryTag.string());
                    }
                }
            }
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            HoverEvent.ShowEntity entity = (HoverEvent.ShowEntity) value;
            writer.writeStringTag("type", entity.type().asString());
            var uuid = entity.id();
            writer.writeIntArrayTag("id", new int[]{
                (int) (uuid.getMostSignificantBits() >> 32),
                (int) uuid.getMostSignificantBits(),
                (int) (uuid.getLeastSignificantBits() >> 32),
                (int) uuid.getLeastSignificantBits()
            });
            Component entityName = entity.name();
            if (entityName != null) {
                writer.writeCompoundTag("name");
                serializeComponent(writer, entityName);
                writer.writeEndTag(); // close name
            }
        }
        writer.writeEndTag(); // close contents
        writer.writeEndTag(); // close hoverEvent
    }

    public static void serializeScoreComponent(MNBTWriter writer, ScoreComponent scoreComponent) {
        writer.writeStringTag("type", "score");
        writer.writeCompoundTag("score");
        writer.writeStringTag("name", scoreComponent.name());
        writer.writeStringTag("objective", scoreComponent.objective());
        writer.writeEndTag(); // close score
    }

    public static void serializeSelectorComponent(MNBTWriter writer, SelectorComponent selectorComponent) {
        writer.writeStringTag("type", "selector");
        writer.writeStringTag("selector", selectorComponent.pattern());
        var separator = selectorComponent.separator();
        if (separator != null) {
            writer.writeCompoundTag("separator");
            serializeComponent(writer, separator);
            writer.writeEndTag();
        }
    }

    public static void serialize(MNBTWriter writer, NBTComponent<?, ?> nbtComponent) {
        writer.writeStringTag("type", "nbt");
        writer.writeStringTag("nbt", nbtComponent.nbtPath());
        writer.writeByteTag("interpret", (byte) (nbtComponent.interpret() ? 1 : 0));
        Component separator = nbtComponent.separator();
        if (separator != null) {
            writer.writeCompoundTag("separator");
            serializeComponent(writer, separator);
            writer.writeEndTag();
        }
        if (nbtComponent instanceof BlockNBTComponent blockNBTComponent) {
            writer.writeStringTag("block", blockNBTComponent.pos().asString());
        } else if (nbtComponent instanceof EntityNBTComponent entityNBTComponent) {
            writer.writeStringTag("entity", entityNBTComponent.selector());
        } else if (nbtComponent instanceof StorageNBTComponent storageNBTComponent) {
            writer.writeStringTag("storage", storageNBTComponent.storage().asString());
        }
    }
}
