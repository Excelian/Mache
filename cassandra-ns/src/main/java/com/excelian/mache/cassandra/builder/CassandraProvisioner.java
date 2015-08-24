package com.excelian.mache.cassandra.builder;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.cassandra.DefaultCassandraConfig;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by jbowkett on 11/08/15.
 */
public class CassandraProvisioner implements StorageProvisioner {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraProvisioner.class);

    @Override
    public String getStorage() {
        return "Cassandra";
    }

    @Override
    public <K, V> Mache<K, V> getCache(String keySpace, Class<V> valueType, SchemaOptions schemaOption, ClusterDetails clusterDetails, StorageServerDetails... serverDetails) {
        final Cluster cluster = getCluster(serverDetails[0], clusterDetails);
        final CassandraCacheLoader<K, V> cacheLoader = getCacheLoader(keySpace, cluster, valueType, schemaOption);

        final MacheFactory macheFactory = new MacheFactory();
        return macheFactory.create(cacheLoader);
    }

    private Cluster getCluster(StorageServerDetails server, ClusterDetails clusterDetails) {
        LOG.info("Connecting to Cassandra cluster...");
        final Cluster cluster = CassandraCacheLoader.connect(server.getAddress(), clusterDetails.getName(), server.getPort(), new DefaultCassandraConfig());
        LOG.info("Connected.");
        return cluster;
    }


    private <K, V> CassandraCacheLoader<K, V> getCacheLoader(String keySpace, Cluster cluster, Class<V> valueTypes, SchemaOptions createanddropschema) {
        LOG.info("Creating cache loader with keyspace:[" + keySpace + "]");
        final CassandraCacheLoader<K, V> cacheLoader = new CassandraCacheLoader<>(
            valueTypes, cluster, createanddropschema, keySpace, new DefaultCassandraConfig());
        LOG.info("CacheLoader created.");
        return cacheLoader;
    }
}
