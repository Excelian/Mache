package com.excelian.mache.jmeter.cassandra.knownKeys;


import com.datastax.driver.core.Cluster;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.cassandra.AbstractCassandraSamplerClient;
import com.excelian.mache.jmeter.cassandra.CassandraTestEntity;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandraConnectionContext;

public class ReadFromDB extends AbstractCassandraSamplerClient {
    private static final long serialVersionUID = 251140199032740124L;
    private CassandraCacheLoader<String, CassandraTestEntity> db;
    private ConnectionContext<Cluster> connectionContext;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromDB.setupTest");

        final Map<String, String> mapParams = extractParameters(context);

        try {
            connectionContext = cassandraConnectionContext(Cluster.builder().withClusterName("BluePrint")
                    .addContactPoint(mapParams.get("cassandra.server.ip.address")).withPort(9042));

            db = cassandra()
                    .withConnectionContext(connectionContext)
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
            readDocumentFromDb(extractParameters(context));
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

    private void readDocumentFromDb(final Map<String, String> params) throws Exception {
        final String docNumber = params.get("entity.keyNo");
        final String key = "document_" + docNumber;
        db.load(key);
    }
}
