package com.excelian.mache.directory.loader.builder;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.directory.loader.DirectoryCacheLoader;
import com.excelian.mache.directory.loader.JsonDirectoryCacheLoader;

/**
 * Provisions directory backed Cache instances with JSON data.
 */
public class JsonDirectoryProvisioner implements StorageProvisioner<String, String> {

    private DirectoryCacheLoader cacheLoader;

    /**
     * Creates a JSON cache loader for the specified cacheLoader.
     * @param cacheLoader The cache loader to use.
     */
    public JsonDirectoryProvisioner(DirectoryCacheLoader cacheLoader) {

        this.cacheLoader = cacheLoader;
    }

    @Override
    public MacheLoader<String, String> getCacheLoader(Class<String> keyType, Class<String> valueType) {
        return new JsonDirectoryCacheLoader(cacheLoader);
    }
}
