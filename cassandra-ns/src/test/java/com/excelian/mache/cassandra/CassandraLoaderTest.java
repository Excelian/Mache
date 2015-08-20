package com.excelian.mache.cassandra;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.datastax.driver.core.Cluster;
import com.excelian.mache.ExCacheLoader;
import com.excelian.mache.NoRunningCassandraDbForTests;
import com.excelian.mache.SchemaOptions;
import org.junit.Rule;

/**
 * Created on 14/07/2015.
 */
@IgnoreIf(condition = NoRunningCassandraDbForTests.class)
public class CassandraLoaderTest extends TestCacheLoaderBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected ExCacheLoader buildCacheLoader(Class cls) throws Exception {
        Cluster cluster = CassandraCacheLoader.connect(new NoRunningCassandraDbForTests().HostName(), "BluePrint", 9042);
        return new CassandraCacheLoader<String, TestEntity>(cls, cluster, SchemaOptions.CREATEANDDROPSCHEMA, keySpace);
    }
}
