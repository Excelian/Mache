package com.excelian.mache.chroniclemap.chroniclemap;

import com.excelian.mache.core.InMemoryCacheLoader;
import com.excelian.mache.core.Mache;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChronicleMapMacheProvisionerTest {

    @Test
    public void testCreate() throws Exception {
        Mache<String, String> provisioner =
                ChronicleMapMacheProvisioner.chronicleMap(ChronicleMapBuilder.of(String.class, String.class)
                        .entries(10)).create().create(String.class, String.class, new InMemoryCacheLoader<>(String.class));

        for (int i = 0; i < 30; i++) {
            provisioner.put("" + i, "" + i);
        }

    }
}