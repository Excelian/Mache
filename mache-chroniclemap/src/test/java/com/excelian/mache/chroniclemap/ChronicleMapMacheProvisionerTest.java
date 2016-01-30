package com.excelian.mache.chroniclemap;

import com.excelian.mache.builder.MacheBuilder;
import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.Mache;
import junit.framework.Assert;
import org.junit.Test;

import static com.excelian.mache.chroniclemap.ChronicleMapMacheProvisioner.chronicleMap;
import static junit.framework.Assert.assertNotNull;

public class ChronicleMapMacheProvisionerTest {


    @Test
    public void testCreate() throws Exception {
        Mache<String, Integer> mache = MacheBuilder.mache(String.class, Integer.class)
                .cachedBy(chronicleMap(String.class, Integer.class))
                .storedIn((keyType, valueType) -> new HashMapCacheLoader<>(valueType))
                .withNoMessaging()
                .macheUp();
        
        assertNotNull(mache);
    }


}