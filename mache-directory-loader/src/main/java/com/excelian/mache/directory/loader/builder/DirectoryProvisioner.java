package com.excelian.mache.directory.loader.builder;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.directory.loader.DirectoryAccessor;
import com.excelian.mache.directory.loader.DirectoryCacheLoader;

import java.nio.ByteBuffer;

/**
 * Provisions directory backed Cache instances.
 */
public class DirectoryProvisioner implements StorageProvisioner<String, ByteBuffer> {
    private DirectoryCacheLoader directoryCacheLoader;

    public DirectoryProvisioner(DirectoryCacheLoader directoryCacheLoader) {
        this.directoryCacheLoader = directoryCacheLoader;
    }

    @Override
    public MacheLoader<String, ByteBuffer> getCacheLoader(Class<String> keyType, Class<ByteBuffer> valueType) {
        return directoryCacheLoader;
    }

    /**
     * Create a directory provisioner with specified constraints.
     * @return The builder to construct the new instance
     */
    public static DirectoryAccessorBuilder directoryProvisioner() {
        return (accessor) -> new CompleteBuilder() {
            @Override
            public DirectoryProvisioner build() {
                return new DirectoryProvisioner(new DirectoryCacheLoader(accessor));
            }

            @Override
            public JsonDirectoryProvisioner asJsonDocuments() {
                return new JsonDirectoryProvisioner(new DirectoryCacheLoader(accessor));
            }
        };
    }

    /**
     * Provide a directory cache loader.
     */
    public interface DirectoryAccessorBuilder {
        CompleteBuilder withDirectoryCacheLoader(DirectoryAccessor accessor);
    }

    /**
     * Build the completed instance.
     */
    public interface CompleteBuilder {
        DirectoryProvisioner build();

        JsonDirectoryProvisioner asJsonDocuments();
    }

}
