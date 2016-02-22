package com.excelian.mache.directory.loader;

import com.excelian.mache.core.MacheLoader;

import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * JSON Implementation of a Directory Cache loader.
 * This will wrap a directory cache loader and format output as base64 json payload.
 */
public class JsonDirectoryCacheLoader implements MacheLoader<String, String> {

    private DirectoryCacheLoader cacheLoader;

    /**
     * Creates the JSON Directory loader with the wrapped internal cache loader
     * @param cacheLoader The cache loader to wrap
     */
    public JsonDirectoryCacheLoader(DirectoryCacheLoader cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    @Override
    public void create() {
        cacheLoader.create();
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("JSON Directory Cache Loader is read only");
    }

    @Override
    public void remove(String key) {
        throw new UnsupportedOperationException("JSON Directory Cache Loader is read only");
    }

    @Override
    public String load(String key) throws Exception {
        ByteBuffer load = cacheLoader.load(key);

        if (load == null) {
            return null;
        } else {
            String base64 = Base64.getEncoder().encodeToString(load.array());
            return "{ \"payload\": \"" + base64 + "\" }";
        }
    }

    @Override
    public void close() {
        cacheLoader.close();
    }

    @Override
    public String getName() {
        return JsonDirectoryCacheLoader.class.getName();
    }
}
