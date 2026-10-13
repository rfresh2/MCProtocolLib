package org.geysermc.adventure.text.serializer.nbt;

import com.viaversion.nbt.tag.CompoundTag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;
import java.util.UUID;

public class ResolvableProfileSerializerTest {
    // List of lovely people
    static final List<Arguments> PROFILES = List.of(
        Arguments.arguments(
            TestTags.compound(),
            ObjectContents.playerHead()
        ),
        Arguments.arguments(
            TestTags.compound("name", "AuriiU"),
            ObjectContents.playerHead().name("AuriiU")
        ),
        Arguments.arguments(
            TestTags.compound("id", new int[]{-317322360, 1695630390, -1770067371, 257805128}),
            ObjectContents.playerHead().id(new UUID(-1362889156793908170L, -7602381469903893688L))
        ),
        Arguments.arguments(
            TestTags.compound(
                "name", "GeyserMC",
                "id", new int[]{568551383, 634342971, -1301730230, 805981675},
                "properties", TestTags.list(
                    TestTags.compound(
                        "name", "textures",
                        "value", "ewogICJ0aW1lc3RhbXAiIDogMTc4NzY3Mzk4NDI2NSwKICAicHJvZmlsZUlkIiA6ICIyMWUzNjdkNzI1Y2Y0ZTNiYjI2OTJjNGEzMDBhNGRlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHZXlzZXJNQyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzI3MGM3ODgyY2E3YjIyZTUzNTg1MTFjOTEwNmI5ODA0NDc1NjUzM2NkZTJiMTNlNmM3YmU3ZmMxNjliYjA2IgogICAgfQogIH0KfQ",
                        "signature", "H8ql2E+/2yARYsWQCb3ty9qEXj7ys+3JjpY+K4yjRjnnxzfyKkokpiuBKc3mO+PReGyFjnaf1smbINa6XKjXFo8GoaeJYMxCjiwMbnVz1O/riAWKsoNyfS54EYK3vZaRV6MZ8ubZDnCrENuzV8I8Oi2rwhzZ24MhTMmChj7RV+F0RJUke8SL5zMTWEqmMC4369KzBSry80ZDnR3Q6dKs4kqj/fJ64pGfh+Zw3nJwntvKIjtzQRM8PbnW1+LXnK84PDlJ+t/fepaHwh9Gnorer+Se5iYeMCyI5PzrxuEwCYXvNyprmKVQ4d9S+OgWVWPwuTgnzjC2smDc+g0GgSMye8Ahp1HF/jGEkppf2Pum2dQVX1s0y8+s4ofnV+RCEzQ4ZyTNjKU+gBkkf95+gJMm+Io7UmqGh27WwohCM8CiSQH60/ctah+i57y+CsZpfWSKoq1hufNDDXx/kj7sy2Alx6tDZIkzXeR8VQIOX1b7oFvt2CCUPi1Y6z0jOK1MU7/yl6mCEyLPb7KFCmrnNM7KqrxuKEl2u9nz6Jddz6vA0QjkjAR0xACnKuV0E6Np6e26YofepftFeBremY/ajH00BSU+rqABgM3ChaguRXVldIfKcWQvWffrV5nYh5KwSI9eDUnE/2PN+LkC/gVZ7LyYSnoH/hQ3yPKeXK/IqRWP0zU="
                    )
                )
            ),
            ObjectContents.playerHead()
                .name("GeyserMC")
                .id(new UUID(2441909596714913339L, -5590888765258576405L))
                .profileProperty(PlayerHeadObjectContents.property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTc4NzY3Mzk4NDI2NSwKICAicHJvZmlsZUlkIiA6ICIyMWUzNjdkNzI1Y2Y0ZTNiYjI2OTJjNGEzMDBhNGRlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHZXlzZXJNQyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzI3MGM3ODgyY2E3YjIyZTUzNTg1MTFjOTEwNmI5ODA0NDc1NjUzM2NkZTJiMTNlNmM3YmU3ZmMxNjliYjA2IgogICAgfQogIH0KfQ",
                    "H8ql2E+/2yARYsWQCb3ty9qEXj7ys+3JjpY+K4yjRjnnxzfyKkokpiuBKc3mO+PReGyFjnaf1smbINa6XKjXFo8GoaeJYMxCjiwMbnVz1O/riAWKsoNyfS54EYK3vZaRV6MZ8ubZDnCrENuzV8I8Oi2rwhzZ24MhTMmChj7RV+F0RJUke8SL5zMTWEqmMC4369KzBSry80ZDnR3Q6dKs4kqj/fJ64pGfh+Zw3nJwntvKIjtzQRM8PbnW1+LXnK84PDlJ+t/fepaHwh9Gnorer+Se5iYeMCyI5PzrxuEwCYXvNyprmKVQ4d9S+OgWVWPwuTgnzjC2smDc+g0GgSMye8Ahp1HF/jGEkppf2Pum2dQVX1s0y8+s4ofnV+RCEzQ4ZyTNjKU+gBkkf95+gJMm+Io7UmqGh27WwohCM8CiSQH60/ctah+i57y+CsZpfWSKoq1hufNDDXx/kj7sy2Alx6tDZIkzXeR8VQIOX1b7oFvt2CCUPi1Y6z0jOK1MU7/yl6mCEyLPb7KFCmrnNM7KqrxuKEl2u9nz6Jddz6vA0QjkjAR0xACnKuV0E6Np6e26YofepftFeBremY/ajH00BSU+rqABgM3ChaguRXVldIfKcWQvWffrV5nYh5KwSI9eDUnE/2PN+LkC/gVZ7LyYSnoH/hQ3yPKeXK/IqRWP0zU="))
        ),
        Arguments.arguments(
            TestTags.compound(
                "name", "Redned",
                "properties", TestTags.list(
                    TestTags.compound(
                        "name", "textures",
                        "value", "ewogICJ0aW1lc3RhbXAiIDogMTc4NzY3NDIzNDM0OCwKICAicHJvZmlsZUlkIiA6ICIwOGNjMGI3YTIzNjc0OWQ5YjIwNjllYjM5MWRhOTFiMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWRuZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzZjA3MzU1NWNkOTFiNTA2NGVmNWEyYmIwYjIzNjY2ZTAzNTMwYTUzNDA5YTg0NGM1ZTRkYTgzNzNiZTEzNyIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0MGMwZTAzZGQyNGExMWIxNWE4YjMzYzJhN2U5ZTMyYWJiMjA1MWIyNDgxZDBiYTdkZWZkNjM1Y2E3YTkzMyIKICAgIH0KICB9Cn0"
                    )
                ),
                "texture", "geyser:special_skin"
            ),
            ObjectContents.playerHead()
                .name("Redned")
                .profileProperty(PlayerHeadObjectContents.property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTc4NzY3NDIzNDM0OCwKICAicHJvZmlsZUlkIiA6ICIwOGNjMGI3YTIzNjc0OWQ5YjIwNjllYjM5MWRhOTFiMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWRuZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzZjA3MzU1NWNkOTFiNTA2NGVmNWEyYmIwYjIzNjY2ZTAzNTMwYTUzNDA5YTg0NGM1ZTRkYTgzNzNiZTEzNyIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0MGMwZTAzZGQyNGExMWIxNWE4YjMzYzJhN2U5ZTMyYWJiMjA1MWIyNDgxZDBiYTdkZWZkNjM1Y2E3YTkzMyIKICAgIH0KICB9Cn0"))
                .texture(Key.key("geyser", "special_skin"))
        )
    );

    @ParameterizedTest
    @FieldSource("PROFILES")
    void testDeserialize(CompoundTag map, PlayerHeadObjectContents.Builder result) {
        Assertions.assertEquals(result.build(), ResolvableProfileSerializerImpl.deserialize(map).build());
    }

    @ParameterizedTest
    @FieldSource("PROFILES")
    void testSerialize(CompoundTag result, PlayerHeadObjectContents.Builder profile) {
        Assertions.assertEquals(result, ResolvableProfileSerializerImpl.serialize(profile.build()));
    }

    @Test
    void testDeserializePlayerName() {
        Assertions.assertEquals(ObjectContents.playerHead("Redned"), ResolvableProfileSerializerImpl.deserialize(TestTags.tag("Redned")).build());
    }

    @Test
    void testDeserializeInvalidProfile() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ResolvableProfileSerializerImpl.deserialize(TestTags.tag(1234)));
    }
}
