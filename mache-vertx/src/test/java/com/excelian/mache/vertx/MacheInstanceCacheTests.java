package com.excelian.mache.vertx;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.MacheLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static org.junit.Assert.assertEquals;

public class MacheInstanceCacheTests {

    private Function<MacheRestRequestContext, RestManagedMache> inMemoryFactory;

    @Before
    public void setup() {
        inMemoryFactory = (request) -> {
            try {
                return new RestManagedMache(mache(String.class, String.class)
                    .cachedBy(guava())
                    .storedIn(new StorageProvisioner() {
                        @Override
                        public <K, V> MacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType) {
                            return new HashMapCacheLoader<>(valueType);
                        }
                    })
                    .withNoMessaging()
                    .macheUp(), TimeUnit.SECONDS.toMillis(5));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    @Test
    public void cacheShouldRemoveMapAfterTimeToLiveExceeded() {
        final Runnable[] delegate = new Runnable[1];
        AtomicInteger callCount = new AtomicInteger();
        MacheInstanceCache cache = new MacheInstanceCache(inMemoryFactory, (x, y) -> {
            callCount.incrementAndGet();
            delegate[0] = y;
        });

        cache.putKey("TestMap", "TestKey", "Hello");
        String key = cache.getKey("TestMap", "TestKey");

        assertEquals("Hello", key);
        assertEquals(1, callCount.get());
        delegate[0].run();

        cache.getKey("TestMap", "TestKey");
        assertEquals(2, callCount.get());
    }

    @Test
    public void cacheShouldRetainInstancesBetweenCalls() {
        AtomicInteger callCount = new AtomicInteger();
        MacheInstanceCache cache = new MacheInstanceCache(inMemoryFactory, (x, y) -> {
            callCount.incrementAndGet();
        });

        cache.putKey("TestMap", "TestKey", "Hello");
        String key = cache.getKey("TestMap", "TestKey");

        assertEquals("Hello", key);
        assertEquals(1, callCount.get());

        cache.putKey("TestMap", "TestKey", "World");
        cache.getKey("TestMap", "TestKey");
        assertEquals(1, callCount.get());
    }
}
