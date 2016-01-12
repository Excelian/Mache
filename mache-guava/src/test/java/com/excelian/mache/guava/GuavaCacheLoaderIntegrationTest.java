package com.excelian.mache.guava;

import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.Cache;
import com.excelian.mache.core.Mache;
import org.junit.Test;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.guava.builder.GuavaProvisioner.guava;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jbowkett on 11/01/2016.
 */
public class GuavaCacheLoaderIntegrationTest {

    @Test
    public void ensureGuavaCanBeInstantiatedAsTheCachingMechanism() throws Exception {
        final StorageProvisioner mockStorage = mock(StorageProvisioner.class);
        when(mockStorage.getCache(any(Class.class), any(Class.class), any(Cache.class))).thenReturn(mock(Mache.class));

        final Mache<String, String> macheInstance = mache(String.class, String.class)
            .cachedBy(guava())
            .backedBy(mockStorage)
            .withNoMessaging()
            .macheUp();
        assertNotNull(macheInstance);
    }
}
