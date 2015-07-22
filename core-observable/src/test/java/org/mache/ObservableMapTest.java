package org.mache;

import org.junit.Test;
import org.mache.utils.UUIDUtils;
import org.mache.coordination.CoordinationEntryEvent;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ObservableMapTest {
    private int fired;
    private final UUIDUtils uuidUtils = new UUIDUtils();

    @Test
    public void testGet() throws Exception {
        @SuppressWarnings("unchecked")
		ExCache<String, String> cache = mock(ExCache.class);
        ObservableMap<String, String> observable = new ObservableMap<>(cache, uuidUtils);
        observable.registerListener(new MapEventListener() {
            @Override
            public void send(CoordinationEntryEvent<?> event) {
                fired++;
            }
        });

        observable.put("a", "b");
        assertEquals(1, fired);
    }

    @Test
    public void testPut() throws Exception {
    	@SuppressWarnings("unchecked")
    	ExCache<String, String> cache = mock(ExCache.class);
        ObservableMap<String, String> observable = new ObservableMap<>(cache, uuidUtils);
        observable.registerListener(new MapEventListener() {
            @Override
            public void send(CoordinationEntryEvent<?> event) {
                fired++;
            }
        });

        observable.get("a");
        assertEquals(0, fired);
    }
}