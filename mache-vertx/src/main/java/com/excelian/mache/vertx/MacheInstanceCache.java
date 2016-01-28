package com.excelian.mache.vertx;

import com.excelian.mache.core.Mache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Manages the Mache instances created by the vertx REST endpoint
 * <p>
 * The methods exposed from this class are threadsafe.
 */
public class MacheInstanceCache {
    private static final Logger LOG = LoggerFactory.getLogger(MacheInstanceCache.class);
    private final ConcurrentMap<String, Mache<String, String>> cacheInstances = new ConcurrentHashMap<>();
    private final Function<MacheRestRequestContext, Mache<String, String>> factory;

    /**
     * Creates an instance cache that will retain the queried instances.
     *
     * @param factory A threadsafe factory to dispense new map instances
     */
    public MacheInstanceCache(Function<MacheRestRequestContext, Mache<String, String>> factory) {
        this.factory = factory;
    }

    private synchronized Mache<String, String> createMap(String mapName) {
        LOG.trace("adding map {} to cache", mapName);
        Mache<String, String> newMache;
        try {
            newMache = factory.apply(new MacheRestRequestContext(mapName));
            cacheInstances.put(mapName, newMache);
        } catch (Exception e) {
            LOG.error("failed adding map {} to cache", mapName, e);
            throw new RuntimeException("Failed to create map", e);
        }
        return newMache;
    }

    /**
     * Puts a key/value into the specified map, if the map does not exist it is created.
     *
     * @param mapName The map
     * @param key   The key
     * @param value The value
     */
    public void putKey(String mapName, String key, String value) {
        Mache<String, String> mache = getMache(mapName);
        mache.put(key, value);
    }

    /**
     * Get the value for the given map and key.
     *
     * @param mapName The map
     * @param key   The key
     * @return The value
     */
    public String getKey(String mapName, String key) {
        Mache<String, String> mache = getMache(mapName);
        return mache.get(key);
    }

    private synchronized Mache<String, String> getMache(String mapName) {
        Mache<String, String> mache = cacheInstances.get(mapName);
        if (mache == null) {
            mache = createMap(mapName);
        }
        return mache;
    }

    /**
     * Removes an entry from the cache.
     *
     * @param mapName The map to remove
     * @param key   The key to remove
     */
    public void removeKey(String mapName, String key) {
        Mache<String, String> mache = getMache(mapName);
        mache.remove(key);
    }
}
