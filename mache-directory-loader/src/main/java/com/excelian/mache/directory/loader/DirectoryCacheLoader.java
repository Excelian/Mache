package com.excelian.mache.directory.loader;

import com.excelian.mache.core.MacheLoader;

import java.nio.ByteBuffer;

/**
 * The directory cache loader returns data using a DirectoryAccessor implementation
 * for instance selecting data from a remote file system exposing a REST interface
 * or a local disk.
 */
public class DirectoryCacheLoader implements MacheLoader<String, ByteBuffer> {

    private DirectoryAccessor directoryAccessor;

    public DirectoryCacheLoader(DirectoryAccessor directoryAccessor) {
        this.directoryAccessor = directoryAccessor;
    }

    @Override
    public void create() {

    }

    @Override
    public void put(String key, ByteBuffer value) {
        // Put not supported
    }

    @Override
    public void remove(String key) {
        // Do not remove from a file system
    }

    @Override
    public ByteBuffer load(String key) throws Exception {
        return directoryAccessor.getFile(key);
    }

    @Override
    public void close() {
        directoryAccessor.close();
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
