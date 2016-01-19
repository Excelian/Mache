package com.excelian.mache.vertx;

import com.excelian.mache.core.Mache;
import com.excelian.mache.factory.MacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the Mache instances created by the vertx REST endpoint
 */
public class MacheInstanceCache {

    private static final Logger LOG = LoggerFactory.getLogger(MacheInstanceCache.class);
    private final Map<String, Mache<String, String>> cacheInstances = new HashMap<>();
    private final MacheFactory factory;

    /**
     * Creates an instance cache that will retain the queried instances
     */
    public MacheInstanceCache(MacheFactory factory) {
        this.factory = factory;
    }

    private Mache<String, String> createMap(String mapId) {
        LOG.trace("adding map {} to cache", mapId);
        Mache<String, String> newMache = null;
        try {
            newMache = factory.create(String.class, String.class);
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
        Mache<String, String> mache = cacheInstances.get(mapId);
        if (mache == null) {
            mache = createMap(mapId);
        }
        mache.put(key, value);
    }

    /**
     * Get the value for the given map and key
     * @param mapId The map
     * @param key The key
     * @return The value
     */
    public String getKey(String mapId, String key) {
        Mache<String, String> mache = cacheInstances.get(mapId);
        if (mache == null) {
            mache = createMap(mapId);
        }
        return mache.get(key);
    }

    /**
     * Removes a map from the instance cache
     * @param mapId The map to remove
     * @return The removed map
     */
    public Mache<String, String> deleteMap(String mapId) {
        // TODO, should a delete remove data or just local copy?
        // update API documentation to reflect
        return cacheInstances.remove(mapId);
    }
}
