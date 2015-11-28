package com.excelian.mache.aggregate.builder;

import com.excelian.mache.aggregate.AggregateCacheLoader;
import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by jbowkett on 19/11/2015.
 */
public class AggregateProvisioner implements StorageProvisioner {


    private final StorageProvisioner[] storageProvisioners;

    public AggregateProvisioner(StorageProvisioner[] storageProvisioners) {
        this.storageProvisioners = storageProvisioners;
    }

    public static StorageProvisioner multipleStores(StorageProvisioner... storageProvisioners) {
        return new AggregateProvisioner(storageProvisioners);
    }

    @Override
    public <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType) {
        final MacheFactory macheFactory = new MacheFactory();
        return macheFactory.create(getCacheLoader(keyType, valueType));
    }

    @Override
    public <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        final List<AbstractCacheLoader<K, V, ?>> cacheLoaders =
            Arrays.stream(storageProvisioners)
                .map(storageProvisioner -> storageProvisioner.getCacheLoader(keyType, valueType))
                .collect(Collectors.toList());
        return new AggregateCacheLoader<>(cacheLoaders);
    }
}
