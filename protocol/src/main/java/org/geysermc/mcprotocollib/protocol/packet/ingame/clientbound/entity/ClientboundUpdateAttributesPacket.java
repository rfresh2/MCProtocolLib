package org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.data.game.Identifier;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.Attribute;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.AttributeModifier;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.AttributeType;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@AllArgsConstructor
public class ClientboundUpdateAttributesPacket implements MinecraftPacket {
    private final int entityId;
    private final @NonNull List<Attribute> attributes;

    public ClientboundUpdateAttributesPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.entityId = helper.readVarInt(in);
        int length = helper.readVarInt(in);
        this.attributes = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            String key = helper.readString(in);
            double value = in.readDouble();
            int len = helper.readVarInt(in);
            List<AttributeModifier> modifiers = new ArrayList<>(len);
            for (int ind = 0; ind < len; ind++) {
                modifiers.add(new AttributeModifier(helper.readUUID(in), in.readDouble(), helper.readModifierOperation(in)));
            }

            AttributeType type = AttributeType.Builtin.BUILTIN.computeIfAbsent(Identifier.formalize(key), AttributeType.Custom::new);
            this.attributes.add(new Attribute(type, value, modifiers));
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.entityId);
        helper.writeVarInt(out, this.attributes.size());
        for (int i = 0; i < this.attributes.size(); i++) {
            Attribute attribute = this.attributes.get(i);
            helper.writeString(out, attribute.getType().getIdentifier());
            out.writeDouble(attribute.getValue());
            helper.writeVarInt(out, attribute.getModifiers().size());
            for (int j = 0; j < attribute.getModifiers().size(); j++) {
                AttributeModifier modifier = attribute.getModifiers().get(j);
                helper.writeUUID(out, modifier.getUuid());
                out.writeDouble(modifier.getAmount());
                helper.writeModifierOperation(out, modifier.getOperation());
            }
        }
    }
}
