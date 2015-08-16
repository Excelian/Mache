package org.mache.couchbase;

import com.couchbase.client.java.env.CouchbaseEnvironment;
import org.mache.SchemaOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CouchbaseConfig  {

    private Class cacheType;
    private CouchbaseEnvironment couchbaseEnvironment;
    private String adminUser;
    private String adminPassword;
    private List<String> serverAddresses;
    private String bucketName;
    private String bucketPassword;
    private int bucketSize;
    private int numReplicas;
    private boolean flushEnabled;
    private SchemaOptions schemaOptions;

    private CouchbaseConfig(Class cacheType, CouchbaseEnvironment couchbaseEnvironment, String adminUser, String adminPassword,
                            List<String> serverAddresses, String bucketName, String bucketPassword,
                            int bucketSize, int numReplicas, boolean flushEnabled, SchemaOptions schemaOptions) {
        this.cacheType = cacheType;
        this.couchbaseEnvironment = couchbaseEnvironment;
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
        private List<String> serverAddresses = new ArrayList<>(Collections.singletonList("localhost"));
        private String bucketName;
        private String bucketPassword = "";
        private int bucketSize = 512;
        private int numReplicas = 0;
        private boolean flushEnabled = false;
        private SchemaOptions schemaOptions = SchemaOptions.USEEXISTINGSCHEMA;
        private CouchbaseEnvironment couchbaseEnvironment;

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
            this.serverAddresses = serverAddresses;
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

        public Builder withCouchbaseEnvironment(CouchbaseEnvironment couchbaseEnvironment) {
            this.couchbaseEnvironment = couchbaseEnvironment;
            return this;
        }

        public CouchbaseConfig build() {
            if (bucketName == null || bucketName.isEmpty()) {
                throw new IllegalArgumentException("Must provide bucket name.");
            } else if (cacheType == null) {
                throw new IllegalArgumentException("Must provide cache type.");
            }

            return new CouchbaseConfig(cacheType, couchbaseEnvironment, adminUser, adminPassword, serverAddresses,
                    bucketName, bucketPassword, bucketSize, numReplicas, flushEnabled, schemaOptions);
        }
    }


    public static CouchbaseConfig.Builder builder() {
        return new Builder();
    }


    @SuppressWarnings("unchecked")
    public <V> Class<V> getCacheType() {
        return cacheType;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public List<String> getServerAddresses() {
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

    public CouchbaseEnvironment getCouchbaseEnvironment() {
        return couchbaseEnvironment;
    }
}
