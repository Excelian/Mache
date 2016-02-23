package com.excelian.mache.directory.loader;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * The file system is able to retrieve files using the Java File API.
 */
public class FileSystemDirectoryAccessor implements DirectoryAccessor {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemDirectoryAccessor.class);

    private File rootDirectory;

    public FileSystemDirectoryAccessor(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    @Nullable
    public ByteBuffer getFile(String file) {
        File resolvedFile = new File(rootDirectory, file);
        try (RandomAccessFile fileHandle = new RandomAccessFile(resolvedFile, "r");
             FileChannel channel = fileHandle.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            return buffer;
        } catch (IOException e) {
            LOG.error("Failed to read file " + file, e);
        }
        return null;
    }
}
