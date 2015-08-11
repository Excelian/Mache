package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.datastax.driver.core.Cluster;

import org.junit.Rule;

/**
 * Created on 14/07/2015.
 */
@IgnoreIf(condition = CassandraDbForTestsPresent.class)
public class CassandraLoaderTest extends TestCacheLoaderBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected ExCacheLoader buildCacheLoader(Class cls) throws Exception {
        Cluster cluster = CassandraCacheLoader.connect(CassandraDbForTestsPresent.HostName(), "BluePrint", 9042);
        return new CassandraCacheLoader<String,TestEntity>(cls, cluster, SchemaOptions.CREATEANDDROPSCHEMA, keySpace);
    }
}
