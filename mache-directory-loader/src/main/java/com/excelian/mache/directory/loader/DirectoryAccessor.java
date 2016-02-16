package com.excelian.mache.directory.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Provides access to a directory of files were the names of files are the map keys
 * and the contents are the values.
 */
public interface DirectoryAccessor {

    /**
     * List all files within the specified directory.
     *
     * @param directory The directory
     * @return A list of files within the directory
     */
    @NotNull
    List<String> listFiles(String directory);

    /**
     * Loads the specified files into a buffer.
     *
     * @param file The file
     * @return Loaded ByteBuffer or null if file loading fails
     */
    @Nullable
    ByteBuffer getFile(String file);

    /**
     * Close the connection to the directory.
     */
    void close();
}
