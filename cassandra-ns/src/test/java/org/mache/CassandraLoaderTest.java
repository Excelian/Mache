package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.datastax.driver.core.Cluster;
import org.junit.Rule;

/**
 * Created on 14/07/2015.
 */
@IgnoreIf(condition = NotRunningInExcelian.class)
public class CassandraLoaderTest extends TestCacheLoaderBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected ExCacheLoader buildCacheLoader(Class cls) throws Exception {
        Cluster cluster = CassandraCacheLoader.connect("10.28.1.140", "BluePrint", 9042);
        return new CassandraCacheLoader<String,TestEntity>(cls, cluster, SchemaOptions.CREATEANDDROPSCHEMA, keySpace);
    }
}
