package com.excelian.mache.directory.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<String> listFiles(String directory) {
        File[] files = new File(rootDirectory, directory).listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(files)
            .stream().map(File::getName).collect(Collectors.toList());
    }

    @Override
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

    @Override
    public void close() {

    }
}
