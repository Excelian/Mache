package com.excelian.mache.hadoop;

import com.excelian.mache.directory.loader.ByteBufferUtilities;
import com.excelian.mache.directory.loader.DirectoryAccessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Hadoop implementation of the DirectoryAccessor.
 */
public class HadoopDirectoryAccessor implements DirectoryAccessor {
    private static final Logger LOG = LoggerFactory.getLogger(HadoopDirectoryAccessor.class);

    private final FileSystem fileSystem;
    private Path path;

    /**
     * Initialise hadoop with the specified path.
     *
     * @param configuration The configuration to use.
     * @param path          The path to initialise hadoop with
     */
    public HadoopDirectoryAccessor(Configuration configuration, Path path) {
        this.path = path;

        try {
            fileSystem = FileSystem.get(configuration);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialise hadoop file system", e);
        }
    }

    @Nullable
    @Override
    public ByteBuffer getFile(String file) {
        try (FSDataInputStream open = fileSystem.open(new Path(path, file))) {
            return ByteBufferUtilities.readInputStream(open);
        } catch (Exception e) {
            LOG.error("Failed to retrieve Hadoop file " + file, e);
        }
        return null;
    }
}
