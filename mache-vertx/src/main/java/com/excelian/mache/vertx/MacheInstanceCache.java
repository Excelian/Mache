package com.excelian.mache.vertx;

import com.excelian.mache.core.Mache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Manages the Mache instances created by the vertx REST endpoint
 */
public class MacheInstanceCache {

    private static final Logger LOG = LoggerFactory.getLogger(MacheInstanceCache.class);
    private final Map<String, Mache<String, String>> cacheInstances = new HashMap<>();
    private final Supplier<Mache<String, String>> factory;

    /**
     * Creates an instance cache that will retain the queried instances
     */
    public MacheInstanceCache(Supplier<Mache<String, String>> factory) {
        this.factory = factory;
    }

    private Mache<String, String> createMap(String mapId) {
        LOG.trace("adding map {} to cache", mapId);
        Mache<String, String> newMache = null;
        try {
            newMache = factory.get();
            cacheInstances.put(mapId, newMache);
        } catch (Exception e) {
            LOG.error("failed adding map {} to cache", mapId, e);
            throw new RuntimeException("Failed to create map", e);
        }
        return newMache;
    }

    /**
     * Puts a key/value into the specified map, if the map does not exist it is created
     * @param mapId The map
     * @param key The key
     * @param value The value
     */
    public void putKey(String mapId, String key, String value) {
        Mache<String, String> mache = getMache(mapId);
        mache.put(key, value);
    }

    /**
     * Get the value for the given map and key
     * @param mapId The map
     * @param key The key
     * @return The value
     */
    public String getKey(String mapId, String key) {
        Mache<String, String> mache = getMache(mapId);
        return mache.get(key);
    }

    private Mache<String, String> getMache(String mapId) {
        Mache<String, String> mache = cacheInstances.get(mapId);
        if (mache == null) {
            mache = createMap(mapId);
        }
        return mache;
    }

    /**
     * Removes an entry from the cache
     * @param mapId The map to remove
     * @param key The key to remove
     */
    public void removeKey(String mapId, String key) {
        Mache<String, String> mache = getMache(mapId);
        mache.remove(key);
    }
}
