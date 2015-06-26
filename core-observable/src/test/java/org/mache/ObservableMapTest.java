package org.mache;

import org.junit.Test;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class ObservableMapTest {

    private int fired;

    @Test
    public void testGet() throws Exception {

        ExCache<String, String> cache = mock(ExCache.class);
        ObservableMap<String, String> observable = new ObservableMap<>(cache);
        observable.registerListener(updated -> fired++);
        observable.put("a", "b");
        assertEquals(1, fired);
    }

    @Test
    public void testPut() throws Exception {
        ExCache<String, String> cache = mock(ExCache.class);
        ObservableMap<String, String> observable = new ObservableMap<>(cache);
        observable.registerListener(updated -> fired++);
        observable.get("a");
        assertEquals(0, fired);
    }
}