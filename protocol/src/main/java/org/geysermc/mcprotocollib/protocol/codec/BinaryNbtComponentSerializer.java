package org.geysermc.mcprotocollib.protocol.codec;

import com.google.gson.internal.LazilyParsedNumber;
import com.viaversion.nbt.mini.MNBT;
import com.viaversion.nbt.mini.MNBTWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static org.slf4j.LoggerFactory.getLogger;

public class BinaryNbtComponentSerializer {
    private static final Logger LOGGER = getLogger("Proxy");

    public static MNBT serializeToMNBT(Component component) {
//        component = component.compact();
        try (MNBTWriter writer = new MNBTWriter()) {
            writer.writeStartTag();
            serialize(writer, component);
            writer.writeEndTag();
            return writer.toMNBT();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void serializeMNBTToBuffer(Component component, DataOutputStream buf) {
        try (MNBTWriter writer = new MNBTWriter(buf)) {
            writer.writeStartTag();
            serialize(writer, component);
            writer.writeEndTag();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void serialize(MNBTWriter writer, TextComponent component) {
        writer.writeStringTag("text", component.content());
    }

    public static void serialize(MNBTWriter writer, TranslatableComponent component) {
        writer.writeStringTag("translate", component.key());
        if (component.fallback() != null) {
            writer.writeStringTag("fallback", component.fallback());
        }
        if (!component.arguments().isEmpty()) {
            if (component.arguments().size() == 1) {
                var arg = component.arguments().get(0);
                var argValue = arg.value();
                if (argValue instanceof Component) {
                    writer.writeListTag("with", 10, 1);
                    serialize(writer, (Component) argValue);
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
                        writer.writeListTag("with", 4, 1);writer.writeLongTag((Long) argValue);
                    } else if (argValue instanceof Float) {
                        writer.writeListTag("with", 5, 1);
                        writer.writeFloatTag((Float) argValue);
                    } else if (argValue instanceof Double) {
                        writer.writeListTag("with", 6, 1);
                        writer.writeDoubleTag((Double) argValue);
                    }
                }
            } else {
                writer.writeListTag("with", 10, component.arguments().size());
                for (int i = 0; i < component.arguments().size(); i++) {
                    var arg = component.arguments().get(i);
                    var argValue = arg.value();
                    if (argValue instanceof Component) {
                        serialize(writer, (Component) argValue);
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

    public static void serialize(MNBTWriter writer, Component component) {
        if (component instanceof TextComponent textComponent) {
            writer.writeStringTag("type", "text");
            serialize(writer, textComponent);
        } else if (component instanceof TranslatableComponent translatableComponent) {
            writer.writeStringTag("type", "translatable");
            serialize(writer, translatableComponent);
        } else {
            LOGGER.error("BinaryNbtComponentSerializer: Unknown component type: {}", component.getClass().getName());
        }
        if (!component.style().isEmpty()) {
            if (component.style().color() != null) {
                var color = component.style().color();
                if (color instanceof NamedTextColor namedTextColor) {
                    writer.writeStringTag("color", namedTextColor.toString());
                } else {
                    writer.writeStringTag("color", component.style().color().asHexString());
                }
            }
            if (component.style().hasDecoration(TextDecoration.BOLD)) {
                writer.writeByteTag("bold", (byte) 1);
            }
            if (component.style().hasDecoration(TextDecoration.ITALIC)) {
                writer.writeByteTag("italic", (byte) 1);
            }
            if (component.style().hasDecoration(TextDecoration.UNDERLINED)) {
                writer.writeByteTag("underlined", (byte) 1);
            }
            if (component.style().hasDecoration(TextDecoration.STRIKETHROUGH)) {
                writer.writeByteTag("strikethrough", (byte) 1);
            }
            if (component.style().hasDecoration(TextDecoration.OBFUSCATED)) {
                writer.writeByteTag("obfuscated", (byte) 1);
            }
            if (component.style().clickEvent() != null) {
                writer.writeCompoundTag("clickEvent");
                writer.writeStringTag("action", component.style().clickEvent().action().toString());
                writer.writeStringTag("value", component.style().clickEvent().value());
                writer.writeEndTag();
            }
            if (component.style().hoverEvent() != null) {
                var hover = component.style().hoverEvent().value();
                writer.writeCompoundTag("hoverEvent");
                writer.writeStringTag("action", component.style().hoverEvent().action().toString());
                writer.writeCompoundTag("contents");
                if (hover instanceof Component hoverComponent) {
                    serialize(writer, hoverComponent);
                } else if (hover instanceof HoverEvent.ShowItem showItem) {
                    writer.writeStringTag("id", showItem.item().asString());
                    if (showItem.count() != 1)
                        writer.writeIntTag("count", showItem.count());
                    if (showItem.nbt() != null) {
                        writer.writeStringTag("tag", showItem.nbt().string()); // todo: ??
                    }
                } else if (hover instanceof HoverEvent.ShowEntity showEntity) {
                    writer.writeStringTag("type", showEntity.type().asString());
                    var uuid = showEntity.id();
                    writer.writeIntArrayTag("id", new int[]{
                        (int) (uuid.getMostSignificantBits() >> 32),
                        (int) uuid.getMostSignificantBits(),
                        (int) (uuid.getLeastSignificantBits() >> 32),
                        (int) uuid.getLeastSignificantBits()
                    });
                    if (showEntity.name() != null) {
                        writer.writeCompoundTag("name");
                        serialize(writer, showEntity.name());
                        writer.writeEndTag();
                    }
                } else {
                    writer.writeStringTag("value", hover.toString());
                }
                writer.writeEndTag();
                writer.writeEndTag();
            }
            if (component.style().insertion() != null) {
                writer.writeStringTag("insertion", component.style().insertion());
            }
            if (component.style().font() != null) {
                writer.writeStringTag("font", component.style().font().asString());
            }
        }
        if (!component.children().isEmpty()) {
            writer.writeListTag("extra", 10, component.children().size());
            for (Component child : component.children()) {
                serialize(writer, child);
                writer.writeEndTag();
            }
        }
    }

    public static Component deserializeFromMNBT(MNBT nbt) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(nbt.getData()))) {
            var builder = Component.text();
            // TODO: Implement deserialize
//            deserialize(in, builder);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

