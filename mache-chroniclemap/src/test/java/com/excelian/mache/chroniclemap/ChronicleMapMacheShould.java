package com.excelian.mache.chroniclemap;

import com.excelian.mache.builder.MacheBuilder;
import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.excelian.mache.chroniclemap.ChronicleMapMacheProvisioner.chronicleMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ChronicleMapMacheShould {

    Mache<String, String> mache;
    private MacheLoader<String, String> loader;

    @Before
    public void setUp() throws Exception {
        loader = Mockito.spy(new HashMapCacheLoader<>(String.class));

        mache = MacheBuilder.mache(String.class, String.class)
                .cachedBy(chronicleMap(String.class, String.class))
                .storedIn((keyType, valueType) -> loader)
                .withNoMessaging()
                .macheUp();
    }

    @After
    public void tearDown() throws Exception {
        mache.close();
    }

    @Test
    public void deferGettingNameToTheCacheLoader() throws Exception {
        verify(loader).getName();
        assertEquals(loader.getName(), mache.getName());
    }

    @Test
    public void setCacheId() throws Exception {
        assertNotNull(mache.getId());
    }

    @Test
    public void readThroughWhenGettingButThenCache() throws Exception {
        loader.put("Hello", "World!");
        assertThat(mache.get("Hello"), is("World!"));
        assertThat(mache.get("Hello"), is("World!"));
        verify(loader, times(1)).load("Hello");
    }

    @Test
    public void writeThroughWhenPutting() throws Exception {
        mache.put("Hello", "World!");
        assertThat(mache.get("Hello"), is("World!"));
        assertThat(mache.get("Hello"), is("World!"));
        verify(loader).put("Hello", "World!");
        verify(loader, times(0)).load("Hello");
    }

    @Test
    public void removesFromBothCacheAndStore() throws Exception {
        mache.put("Hello", "World!");
        mache.remove("Hello");
        assertNull(mache.get("Hello"));
        verify(loader).remove("Hello");
    }

    @Test
    public void beAbleToIvalidateAllEntries() throws Exception {
        mache.put("One", "Value");
        mache.put("Two", "Value");
        mache.invalidateAll();
        assertThat(mache.get("One"), is("Value"));
        assertThat(mache.get("Two"), is("Value"));
        verify(loader).load("One");
        verify(loader).load("Two");
    }

    @Test
    public void beAbleToInvalidateIndividualEntries() throws Exception {
        mache.put("One", "Value");
        mache.put("Two", "Value");
        mache.invalidate("One");
        assertThat(mache.get("One"), is("Value"));
        assertThat(mache.get("Two"), is("Value"));
        verify(loader).load("One");
        verify(loader, times(0)).load("Two");
    }

    @Test
    public void closeTheCacheLoader() throws Exception {
        mache.close();
        verify(loader).close();
    }

    @Test
    public void returnTheCacheLoader() throws Exception {
        assertThat(mache.getCacheLoader(), is(loader));
    }
}