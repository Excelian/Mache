package org.mache.couchbase;

import org.mache.SchemaOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CouchbaseConfig  {

    private Class cacheType;
    private String adminUser;
    private String adminPassword;
    private List<URI> serverAddresses;
    private String bucketName;
    private String bucketPassword;
    private int bucketSize;
    private int numReplicas;
    private boolean flushEnabled;
    private SchemaOptions schemaOptions;

    private CouchbaseConfig(Class cacheType, String adminUser, String adminPassword,
                            List<URI> serverAddresses, String bucketName, String bucketPassword,
                            int bucketSize, int numReplicas, boolean flushEnabled, SchemaOptions schemaOptions) {
        this.cacheType = cacheType;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.serverAddresses = serverAddresses;
        this.bucketName = bucketName;
        this.bucketPassword = bucketPassword;
        this.bucketSize = bucketSize;
        this.numReplicas = numReplicas;
        this.flushEnabled = flushEnabled;
        this.schemaOptions = schemaOptions;
    }

    public static class Builder {
        private Class cacheType;
        private String adminUser = "Administrator";
        private String adminPassword = "password";
        private List<URI> serverAddresses = new ArrayList<>(1);
        private String bucketName;
        private String bucketPassword = "";
        private int bucketSize = 512;
        private int numReplicas = 0;
        private boolean flushEnabled = false;
        private SchemaOptions schemaOptions = SchemaOptions.CREATEANDDROPSCHEMA;

        public Builder withCacheType(Class cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public Builder withAdminUser(String adminUser) {
            this.adminUser = adminUser;
            return this;
        }

        public Builder withAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
            return this;
        }

        public Builder withServerAdresses(List<String> serverAddresses) {
            for (String address : serverAddresses) {
                try {
                    this.serverAddresses.add(new URI(address));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            return this;
        }

        public Builder withBucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder withBucketPassword(String bucketPassword) {
            this.bucketPassword = bucketPassword;
            return this;
        }

        public Builder withBucketSize(int bucketSize) {
            this.bucketSize = bucketSize;
            return this;
        }

        public Builder withNumReplicas(int numReplicas) {
            this.numReplicas = numReplicas;
            return this;
        }

        public Builder withFlushEnabled(boolean flushEnabled) {
            this.flushEnabled = flushEnabled;
            return this;
        }

        public Builder withSchemaOptions(SchemaOptions schemaOptions) {
            this.schemaOptions = schemaOptions;
            return this;
        }

        public CouchbaseConfig build() {
            return new CouchbaseConfig(cacheType, adminUser, adminPassword, serverAddresses,bucketName, bucketPassword,
                    bucketSize, numReplicas, flushEnabled, schemaOptions);
        }
    }


    public static CouchbaseConfig.Builder builder() {
        return new Builder();
    }


    public <V> Class<V> getCacheType() {
        return cacheType;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public List<URI> getServerAddresses() {
        return serverAddresses;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getBucketPassword() {
        return bucketPassword;
    }

    public int getBucketSize() {
        return bucketSize;
    }

    public int getNumReplicas() {
        return numReplicas;
    }

    public boolean isFlushEnabled() {
        return flushEnabled;
    }

    public SchemaOptions getSchemaOptions() {
        return schemaOptions;
    }
}
