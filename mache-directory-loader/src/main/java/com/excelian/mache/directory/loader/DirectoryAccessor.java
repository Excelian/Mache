package com.excelian.mache.directory.loader;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Provides access to a directory of files were the names of files are the map keys
 * and the contents are the values.
 */
public interface DirectoryAccessor {

    /**
     * Loads the specified files into a buffer.
     *
     * @param file The file
     * @return Loaded ByteBuffer or null if file loading fails
     */
    @Nullable
    ByteBuffer getFile(String file);
}
