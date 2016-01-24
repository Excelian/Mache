package com.excelian.mache.chroniclemap;

import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.Mache;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class ChronicleMapMacheProvisionerTest {

    @Test
    public void testCreate() throws Exception {
        Mache<String, String> provisioner =
                ChronicleMapMacheProvisioner.<String, String>chronicleMap()
                        .size(1000)
                        .withRunCleanupThread(true)
                        .create()
                        .create(String.class, String.class, new HashMapCacheLoader<>(String.class));

        for (int i = 0; i < 30000; i++) {
            provisioner.put("" + i, "" + i);
            Thread.yield();
        }
    }
}