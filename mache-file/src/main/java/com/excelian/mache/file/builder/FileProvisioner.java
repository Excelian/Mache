package com.excelian.mache.file.builder;

import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;

import com.excelian.mache.file.FileCacheLoader;

/**
 * Created by jbowkett on 19/11/2015.
 */
public class FileProvisioner implements StorageProvisioner {

    public static StorageProvisioner file() {
        return new FileProvisioner();
    }

    @Override
    public <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType) {
        final MacheFactory macheFactory = new MacheFactory();
        return macheFactory.create(getCacheLoader(keyType, valueType));
    }

    @Override
    public <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        return new FileCacheLoader<>(keyType, valueType);
    }
}
