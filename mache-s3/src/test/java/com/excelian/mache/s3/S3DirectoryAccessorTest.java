package com.excelian.mache.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.excelian.mache.directory.loader.DirectoryCacheLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * S3 tests, using the ruby gem fakes3, the server must be started prior to running tests.
 *
 * For this to work we add a file to the fake s3 and then query the file back.
 */
public class S3DirectoryAccessorTest {
    private static final String BUCKET_NAME = "myBucket";

    private S3DirectoryAccessor directoryAccessor;

    @Before
    public void create() {
        AmazonS3Client s3Client = new AmazonS3Client();
        s3Client.setEndpoint("http://localhost:4567");

        directoryAccessor = new S3DirectoryAccessor(s3Client, BUCKET_NAME);

        // get the trades file within directory tests
        URL resource = S3DirectoryAccessorTest.class.getResource("/Trades/June2016.txt");
        assertNotNull("Resource not found", resource);
        s3Client.putObject(BUCKET_NAME, "trades/myFile.txt", new File(resource.getFile()));
    }

    @Test
    public void shouldListFilesInS3Bucket() {
        List<String> trades = directoryAccessor.listFiles("trades");
        assertEquals(1, trades.size());
    }

    @Test
    public void shouldReadFileFromBucket() {
        ByteBuffer file = directoryAccessor.getFile("trades/myFile.txt");
        assertNotNull(file);
    }

}