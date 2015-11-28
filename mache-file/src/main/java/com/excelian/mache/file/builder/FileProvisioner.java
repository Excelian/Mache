package com.excelian.mache.file.builder;

import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;

import com.excelian.mache.file.FileCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by jbowkett on 19/11/2015.
 */
public class FileProvisioner implements StorageProvisioner {

    private static final Logger log = LoggerFactory.getLogger(FileProvisioner.class);

    private File location;

    public static FileProvisioner file() {
        return new FileProvisioner();
    }

    @Override
    public <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType) {
        final MacheFactory macheFactory = new MacheFactory();
        return macheFactory.create(getCacheLoader(keyType, valueType));
    }

    @Override
    public <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        return new FileCacheLoader<>(keyType, valueType, getLocation());
    }

    public File getLocation() {
        if (!validFile()) {
            final File tempFile = createTempFile();
            log.error("File location not found :[" + this.location + "], using temp file :[" + tempFile + "]");
            this.location = tempFile;
        }
        return this.location;
    }

    private boolean validFile() {
        return this.location != null && !this.location.exists();
    }

    private File createTempFile() {
        try {
            return File.createTempFile("mache-file-cache.", ".mache");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot create file cache:", e);
        }
    }

    public FileProvisioner storedAt(String s) {
        this.location = new File(s);
        return this;
    }
}
