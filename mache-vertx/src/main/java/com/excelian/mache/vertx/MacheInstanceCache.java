package com.excelian.mache.vertx;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.builder.MessagingProvisioner;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.Mache;

import java.util.HashMap;
import java.util.Map;

import static com.excelian.mache.builder.MacheBuilder.mache;

/**
 * Manages the Mache instances created by the vertx REST endpoint
 */
public class MacheInstanceCache {

    private final Map<String, Mache<String, String>> cacheInstances = new HashMap<>();
    // TODO Elements required to create new instances, may be better wrapped behind a factory interface
    // can any element change (e.g. database) without requiring new factory?
    private final StorageProvisioner storageProvisioner;
    private final CacheProvisioner<String, String> cacheProvisioner;
    private final MessagingProvisioner messagingProvisioner;

    /**
     * Creates an instance cache that will create new Mache as required
     */
    public MacheInstanceCache(StorageProvisioner storageProvisioner,
                              CacheProvisioner<String, String> cacheProvisioner,
                              MessagingProvisioner messagingProvisioner) {
        this.storageProvisioner = storageProvisioner;
        this.cacheProvisioner = cacheProvisioner;
        this.messagingProvisioner = messagingProvisioner;
    }

    private Mache<String, String> createMap(String mapId) {
        Mache<String, String> newMache = null;
        try {
            newMache = mache(String.class, String.class)
                    .cachedBy(cacheProvisioner)
                    .storedIn(storageProvisioner)
                    .withMessaging(messagingProvisioner)
                    .macheUp();
            cacheInstances.put(mapId, newMache);
        } catch (Exception e) {
            // TODO log, return failure
            e.printStackTrace();
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
        // Todo, check the map exists
        return cacheInstances.remove(mapId);
    }
}
