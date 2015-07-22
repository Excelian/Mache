package org.mache;

import com.datastax.driver.core.Cluster;

/**
 * Created on 14/07/2015.
 */
public class CassandraLoaderTest extends TestCacheLoaderBase {



    @Override
    protected ExCacheLoader BuildCacheLoader(Class cls) throws Exception {

        Cluster cluster = CassandraCacheLoader.connect("10.28.1.140", "BluePrint", 9042);
        return new CassandraCacheLoader<String,TestEntity>(cls, cluster, SchemaOptions.CREATEANDDROPSCHEMA, keySpace);
    }
}
