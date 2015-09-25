package com.excelian.mache.jmeter.cassandra.knownKeys;

import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;

import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.cassandra.AbstractCassandraSamplerClient;
import com.excelian.mache.jmeter.cassandra.CassandraTestEntity;
import com.excelian.mache.jmeter.mongo.knownKeys.ShuffledSequence;

public class Write1000ToDB extends AbstractCassandraSamplerClient {


    private static final long serialVersionUID = 3550175542777320608L;
    private AbstractCacheLoader<String, CassandraTestEntity, ?> db;
    private ShuffledSequence shuffledSequence = new ShuffledSequence();

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info(getClass().getName() + ".setupTest");

        final Map<String, String> mapParams = extractParameters(context);

        try {
            db = cassandra()
    				.withCluster(Cluster.builder().withClusterName("BluePrint")
    						.addContactPoint(mapParams.get("cassandra.server.ip.address")).withPort(9042).build())
    				.withKeyspace(mapParams.get("keyspace.name")).withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
    				.build().getCacheLoader(String.class, CassandraTestEntity.class);
            db.create();//ensure we are connected and schema exists
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (db != null) {
            db.close();
        }
    }


    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        writeOneThousandDocumentsToDbWithNewData();
        result.sampleEnd();
        result.setSuccessful(true);
        return result;
    }

    private void writeOneThousandDocumentsToDbWithNewData() {
        for (int i : shuffledSequence.upTo(1000)) {
            final String key = "document_" + i;
            final String value = key + "_" + System.currentTimeMillis();
            db.put(key, new CassandraTestEntity(key, value));
        }
    }
}

