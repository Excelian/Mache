package com.excelian.mache.observable;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.core.Mache;
import org.junit.Test;
import com.excelian.mache.observable.utils.UUIDUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ObservableMapTest {
    private int fired;
    private final UUIDUtils uuidUtils = new UUIDUtils();

    @Test
    public void testGet() throws Exception {
        @SuppressWarnings("unchecked")
        Mache<String, String> cache = mock(Mache.class);
        ObservableMap<String, String> observable = new ObservableMap<String, String>(cache, uuidUtils);
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
        Mache<String, String> cache = mock(Mache.class);
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