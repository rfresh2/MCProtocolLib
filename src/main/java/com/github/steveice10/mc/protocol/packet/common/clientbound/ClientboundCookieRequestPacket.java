package com.github.steveice10.mc.protocol.packet.common.clientbound;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

import java.io.IOException;

@Data
@With
@AllArgsConstructor
public class ClientboundCookieRequestPacket implements MinecraftPacket {
    private final String key;

    public ClientboundCookieRequestPacket(ByteBuf in, MinecraftCodecHelper helper) throws IOException {
        this.key = helper.readResourceLocation(in);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) throws IOException {
        helper.writeResourceLocation(out, this.key);
    }
}
