package com.excelian.mache.builder.storage;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;

public interface StorageProvisioner {

    <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType);

}
