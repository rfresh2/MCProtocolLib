package com.github.steveice10.mc.protocol.data.game.entity.object;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FallingBlockData implements ObjectData {
    private final int id;
    private final int metadata;
}
