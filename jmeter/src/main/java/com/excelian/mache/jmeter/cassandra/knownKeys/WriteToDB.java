package com.excelian.mache.jmeter.cassandra.knownkeys;

import com.datastax.driver.core.Cluster;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.cassandra.AbstractCassandraSamplerClient;
import com.excelian.mache.jmeter.cassandra.CassandraTestEntity;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;

/**
 * JMeter test that measures writing directly to the Cassandra backing store.
 */
public class WriteToDB extends AbstractCassandraSamplerClient {
    private static final long serialVersionUID = 4662847886347883622L;
    private MacheLoader<String, CassandraTestEntity> db;
    private ConnectionContext<Cluster> connectionContext;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info(getClass().getName() + ".setupTest");

        final Map<String, String> mapParams = extractParameters(context);

        try {
            final Cluster.Builder bluePrint = Cluster.builder()
                .addContactPoint(mapParams.get("cassandra.server.ip.address"))
                .withPort(9042)
                .withClusterName("BluePrint");
            db = cassandra()
                .withCluster(bluePrint)
                    .withKeyspace(mapParams.get("keyspace.name"))
                    .withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED).build()
                    .getCacheLoader(String.class, CassandraTestEntity.class);
            db.create();// ensure we are connected and schema exists
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (db != null) {
            db.close();
        }

        if (connectionContext != null) {
            try {
                connectionContext.close();
            } catch (Exception e) {
                getLogger().error("Error closing cassandra context", e);
            }
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            writeDocumentToDbWithNewData(extractParameters(context));
            result.sampleEnd();
            result.setSuccessful(true);
        } catch (Exception e) {
            result.sampleEnd();
            result.setSuccessful(false);
            getLogger().error("Error running test", e);
            result.setResponseMessage(e.toString());
        }

        return result;
    }

    private void writeDocumentToDbWithNewData(final Map<String, String> params) {

        final String docNumber = params.get("entity.keyNo");
        final String entityValue = params.get("entity.value");
        final String key = "document_" + docNumber;
        final String value = (entityValue.equals("CURRENTTIME")) ? key + "_" + System.currentTimeMillis() : entityValue;

        getLogger().info("Writing to db key=" + key);
        db.put(key, new CassandraTestEntity(key, value));
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();

        defaultParameters.addArgument("entity.value", "CURRENTTIME");
        return defaultParameters;
    }
}
