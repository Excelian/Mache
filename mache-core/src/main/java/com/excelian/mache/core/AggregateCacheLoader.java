package com.excelian.mache.core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jbowkett on 18/11/2015.
 */
public class AggregateCacheLoader<K, V> extends AbstractCacheLoader<K, V, String> {
    private final List<AbstractCacheLoader<K, V, ?>> cacheLoaders;

    @SuppressWarnings("varargs")
    public AggregateCacheLoader(AbstractCacheLoader<K, V, ?>... cacheLoaders) {
        this.cacheLoaders = new ArrayList<>();
        Arrays.stream(cacheLoaders)
            .forEach(this.cacheLoaders::add);
    }

    @Override
    public void create() {
        this.cacheLoaders.forEach(MacheLoader::create);
    }


    @Override
    public void put(K key, V value) {
        cacheLoaders.forEach(cacheLoader -> cacheLoader.put(key, value));
    }

    @Override
    public void remove(K key) {
        cacheLoaders.forEach(cacheLoader -> cacheLoader.remove(key));
    }

    @Override
    public V load(K key) throws Exception {
        for (AbstractCacheLoader<K, V, ?> cacheLoader : cacheLoaders) {
            final V value = cacheLoader.load(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public void close() {
        this.cacheLoaders.forEach(MacheLoader::close);
    }

    @Override
    public String getName() {
        final String delegateNames = this.cacheLoaders.stream()
            .map(MacheLoader::getName)
            .collect(Collectors.joining(","));
        return "AggregateCacheLoader[" + delegateNames + ']';
    }

    @Override
    public String getDriverSession() {
        return "AggregateCacheLoaderSession";
    }
}
