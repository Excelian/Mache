package com.excelian.mache.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.codeaffine.test.ConditionalIgnoreRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * S3 tests, using the ruby gem fakes3, the server must be started prior to running tests.
 * For this to work we add a file to the fake s3 and then query the file back.
 */
@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningS3ForTests.class)
public class S3DirectoryAccessorTest {
    private static final String BUCKET_NAME = "myBucket";

    private S3DirectoryAccessor directoryAccessor;

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Before
    public void create() {
        AmazonS3Client s3Client = new AmazonS3Client();
        s3Client.setEndpoint(String.format("http://%s:%d", NoRunningS3ForTests.HOST, NoRunningS3ForTests.PORT));

        directoryAccessor = new S3DirectoryAccessor(s3Client, BUCKET_NAME, "trades");

        // get the trades file within directory tests
        URL resource = S3DirectoryAccessorTest.class.getResource("/Trades/June2016.txt");
        assertNotNull("Resource not found", resource);
        s3Client.putObject(BUCKET_NAME, "trades/myFile.txt", new File(resource.getFile()));
    }

    @Test
    public void shouldReadFileFromBucket() {
        ByteBuffer file = directoryAccessor.getFile("myFile.txt");
        assertNotNull(file);
    }

    @Test
    public void shouldReturnNullForMissingFile() {
        ByteBuffer file = directoryAccessor.getFile("not_uploaded.txt");
        assertNull(file);
    }
}