package com.excelian.mache.cassandra;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.NoRunningCassandraDbForTests;
import com.excelian.mache.core.SchemaOptions;
import org.junit.Rule;

/**
 * Created on 14/07/2015.
 */
@IgnoreIf(condition = NoRunningCassandraDbForTests.class)
public class CassandraLoaderTest extends TestCacheLoaderBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected MacheLoader buildCacheLoader(Class cls) throws Exception {
        final DefaultCassandraConfig config = new DefaultCassandraConfig();
        Cluster cluster = CassandraCacheLoader.connect(new NoRunningCassandraDbForTests().HostName(), "BluePrint", 9042, config);
        return new CassandraCacheLoader<String, TestEntity>(cls, cluster, SchemaOptions.CREATEANDDROPSCHEMA, keySpace, config);
    }
}
