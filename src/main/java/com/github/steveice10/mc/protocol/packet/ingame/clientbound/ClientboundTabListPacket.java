package com.github.steveice10.mc.protocol.packet.ingame.clientbound;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.data.DefaultComponentSerializer;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Data
@With
@AllArgsConstructor
public class ClientboundTabListPacket implements MinecraftPacket {
    private final String headerRaw;
    private final @NonNull Component header;
    private final String footerRaw;
    private final @NonNull Component footer;

    public ClientboundTabListPacket(final String headerRaw, final String footerRaw) {
        this.headerRaw = headerRaw;
        this.header = DefaultComponentSerializer.get().deserialize(this.headerRaw);
        this.footerRaw = footerRaw;
        this.footer = DefaultComponentSerializer.get().deserialize(this.footerRaw);
    }

    public ClientboundTabListPacket(final Component header, final Component footer) {
        this.headerRaw = DefaultComponentSerializer.get().serialize(header);
        this.header = header;
        this.footerRaw = DefaultComponentSerializer.get().serialize(footer);
        this.footer = footer;
    }

    public ClientboundTabListPacket(ByteBuf in, MinecraftCodecHelper helper) throws UncheckedIOException {
        this.headerRaw = helper.readString(in, 262144);
        this.header = DefaultComponentSerializer.get().deserialize(this.headerRaw);
        this.footerRaw = helper.readString(in, 262144);
        this.footer = DefaultComponentSerializer.get().deserialize(this.footerRaw);
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) throws IOException {
        helper.writeComponent(out, this.header);
        helper.writeComponent(out, this.footer);
    }
}
