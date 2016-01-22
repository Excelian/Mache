package com.excelian.mache.chroniclemap;

import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ChronicleMapMacheTest {

    Mache<String, String> mache;
    private MacheLoader<String, String> loader;

    @Before
    public void setUp() throws Exception {
        loader = Mockito.spy(new HashMapCacheLoader<>(String.class));

        mache = ChronicleMapMacheProvisioner.<String, String>chronicleMap()
                .create(String.class, String.class, loader);
    }

    @After
    public void tearDown() throws Exception {
        mache.close();
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("String", mache.getName());
    }

    @Test
    public void testGetId() throws Exception {
        assertNotNull(mache.getId());
    }

    @Test
    public void testGetWithReadThrough() throws Exception {
        loader.put("Hello", "World!");
        assertThat(mache.get("Hello"), is("World!"));
        assertThat(mache.get("Hello"), is("World!"));
        verify(loader, times(1)).load("Hello");
    }

    @Test
    public void testPutWithWriteThrough() throws Exception {
        mache.put("Hello", "World!");
        assertThat(mache.get("Hello"), is("World!"));
        assertThat(mache.get("Hello"), is("World!"));
        verify(loader, times(0)).load("Hello");
    }

    @Test(expected = RuntimeException.class)
    public void testRemove() throws Exception {
        mache.put("Hello", "World!");
        mache.remove("Hello");
        mache.get("Hello");
    }

    @Test
    public void testInvalidateAll() throws Exception {
        mache.put("One", "Value");
        mache.put("Two", "Value");
        mache.invalidateAll();
        assertThat(mache.get("One"), is("Value"));
        assertThat(mache.get("Two"), is("Value"));
        verify(loader).load("One");
        verify(loader).load("Two");
    }

    @Test
    public void testInvalidate() throws Exception {
        mache.put("One", "Value");
        mache.put("Two", "Value");
        mache.invalidate("One");
        assertThat(mache.get("One"), is("Value"));
        assertThat(mache.get("Two"), is("Value"));
        verify(loader).load("One");
        verify(loader, times(0)).load("Two");
    }

    @Test
    public void testClose() throws Exception {
        mache.close();
        verify(loader).close();
    }

    @Test
    public void testGetCacheLoader() throws Exception {
        assertThat(mache.getCacheLoader(), is(loader));
    }
}