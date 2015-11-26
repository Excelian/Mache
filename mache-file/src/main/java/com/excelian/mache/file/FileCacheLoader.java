package com.excelian.mache.file;

import com.excelian.mache.core.AbstractCacheLoader;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by jbowkett on 19/11/2015.
 */
public class FileCacheLoader<K, V> extends AbstractCacheLoader<K, V, String> {

    private ChronicleMap<K, V> chronicleMap;

    public FileCacheLoader(Class<K> keyType, Class<V> valueType, File file) {
        try {
            chronicleMap = ChronicleMapBuilder.of(keyType, valueType)
                .createPersistedTo(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot open file for writing:", e);
        }
    }

    /**
     * Creates the underlying store.
     */
    @Override
    public void create() {

    }

    /**
     * Puts the given entry into the underlying store.
     *
     * @param key   the key to put
     * @param value the value to put
     */
    @Override
    public void put(K key, V value) {
        chronicleMap.put(key, value);
    }

    /**
     * Removes the given entry from the store.
     *
     * @param key The key to remove.
     */
    @Override
    public void remove(K key) {
        chronicleMap.remove(key);
    }

    /**
     * Computes or retrieves the value corresponding to {@code key}.
     *
     * @param key the non-null key whose value should be loaded
     * @return the value associated with {@code key}; <b>must not be null</b>
     * @throws Exception            if unable to load the result
     * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
     *                              treated like any other {@code Exception} in all respects except that, when it is caught,
     *                              the thread's interrupt status is set
     */
    @Override
    public V load(K key) throws Exception {
        return chronicleMap.get(key);
    }

    @Override
    public void close() {
        chronicleMap.close();
    }

    @Override
    public String getDriverSession() {
        return getName();
    }

    @Override
    public String getName() {
        return "FileCacheLoader";
    }
}
