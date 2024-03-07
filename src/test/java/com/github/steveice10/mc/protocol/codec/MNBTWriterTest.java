package com.github.steveice10.mc.protocol.codec;

import com.github.steveice10.opennbt.mini.MNBTWriter;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.io.MNBTIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

public class MNBTWriterTest {
    @Test
    public void compoundTagWriteTest() throws Exception {
        var compoundTag = new CompoundTag();
        compoundTag.putInt("testKey1", 1);
        compoundTag.putString("testKey2", "testValue2");
        var listCTag1 = new CompoundTag();
        listCTag1.putString("mapKey1", "mapValue1");
        listCTag1.putString("mapKey2", "mapValue2");
        var listCTag2 = new CompoundTag();
        listCTag2.putString("mapKey3", "mapValue3");
        var listTag = new ListTag(asList(listCTag1, listCTag2));
        compoundTag.put("testKey3", listTag);

        var compoundTagBytes = MNBTIO.write(compoundTag, false).getData();

        try (MNBTWriter writer = new MNBTWriter()) {
            writer.writeStartTag();
            writer.writeIntTag("testKey1", 1);
            writer.writeStringTag("testKey2", "testValue2");
            writer.writeListTag("testKey3", 10, 2);
            writer.writeStringTag("mapKey1", "mapValue1");
            writer.writeStringTag("mapKey2", "mapValue2");
            writer.writeEndTag();
            writer.writeStringTag("mapKey3", "mapValue3");
            writer.writeEndTag();
            writer.writeEndTag();
            var mnbt = writer.toMNBT();
            var mnbtTag = MNBTIO.read(mnbt);
            Assertions.assertEquals(compoundTag, mnbtTag);
        }
    }
}
