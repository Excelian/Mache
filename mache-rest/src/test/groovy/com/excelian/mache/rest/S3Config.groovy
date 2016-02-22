package com.excelian.mache.rest

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.excelian.mache.s3.S3DirectoryAccessor
import java.util.concurrent.TimeUnit

import static com.excelian.mache.builder.MacheBuilder.mache
import static com.excelian.mache.directory.loader.builder.DirectoryProvisioner.directoryProvisioner;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava

/**
 * Run the REST service using a Directory Loader storage provider, the maps created will be evicted after 1 day.
 */
public class S3Config {
    public static void main(String[] args) {
        MacheRestService restService = new MacheRestService();

        restService.runAsync({ context ->
            new RestManagedMache(
                    mache(String.class, String.class)
                            .cachedBy(guava())
                            .storedIn(
                            directoryProvisioner()
                                    .withDirectoryCacheLoader(new S3DirectoryAccessor(Region.getRegion(Regions.EU_WEST_1), context.mapName, ""))
                                    .asJsonDocuments())
                            .withNoMessaging()
                            .macheUp(), TimeUnit.DAYS.toMillis(1));
        });
    }
}