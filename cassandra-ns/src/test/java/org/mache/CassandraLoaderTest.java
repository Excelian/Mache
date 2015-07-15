package org.mache;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.Test;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

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
