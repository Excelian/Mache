package com.excelian.mache.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.excelian.mache.directory.loader.ByteBufferUtilities;
import com.excelian.mache.directory.loader.DirectoryAccessor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * The S3 directory accessor provides access to an S3 bucket.
 */
public class S3DirectoryAccessor implements DirectoryAccessor {
    private static final Logger LOG = LoggerFactory.getLogger(S3DirectoryAccessor.class);

    private final AmazonS3 amazonS3Client;
    private final String bucketName;
    private String prefix;

    /**
     * Creates a new S3 Directory accessor for the specified region.
     * Credentials are required in the correct location, please see the Default Credential Provider here:
     * http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
     *
     * @param region     The region to use S3 within
     * @param bucketName The name of the bucket to use
     * @param prefix     The prefix to apply
     */
    public S3DirectoryAccessor(Region region, String bucketName, String prefix) {
        this.bucketName = bucketName;
        this.prefix = prefix;

        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                "Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (~/.aws/credentials), and is in valid format.",
                e);
        }

        amazonS3Client = new AmazonS3Client(credentials);
        amazonS3Client.setRegion(region);
    }

    /**
     * Creates a new S3 Directory accessor with a given S3 client.
     *
     * @param amazonS3Client The client to use
     * @param bucketName     The bucket to use
     * @param prefix         The file prefix to apply
     */
    public S3DirectoryAccessor(AmazonS3 amazonS3Client, String bucketName, String prefix) {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
        this.prefix = prefix;
    }

    @Nullable
    @Override
    public ByteBuffer getFile(String file) {
        String qualifiedFile = prefix + (prefix.endsWith("/") ? "" : "/") + file;

        try (S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, qualifiedFile))) {
            // Object is null on invalid request
            if (object != null) {
                return ByteBufferUtilities.readInputStream(object.getObjectContent());
            }
        } catch (Exception e) {
            LOG.error("Failed to retrieve file " + file, e);
        }

        return null;
    }
}
