package com.excelian.mache.chroniclemap;

import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.Mache;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class ChronicleMapMacheProvisionerTest {

    @Ignore("Still TODO to prevent users from doing this.")
    @Test
    public void testCreate() throws Exception {
        Mache<String, String> provisioner =
                ChronicleMapMacheProvisioner.chronicleMap(ChronicleMapBuilder.of(String.class, String.class)
                        .entries(10))
                        .create()
                        .create(String.class, String.class, new HashMapCacheLoader<>(String.class));

        for (int i = 0; i < 30; i++) {
            provisioner.put("" + i, "" + i);
        }

    }
}