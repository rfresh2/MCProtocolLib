package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import lombok.Builder;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.data.game.Holder;

@Data
@Builder(toBuilder = true)
public class BannerPatternLayer {
    private final Holder<BannerPattern> pattern;
    private final int colorId;

    @Data
    @Builder(toBuilder = true)
    public static class BannerPattern {
        private final String assetId;
        private final String translationKey;
    }
}
