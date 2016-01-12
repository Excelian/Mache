package com.excelian.mache.jmeter.cassandra.knownkeys;

import com.excelian.mache.jmeter.cassandra.CassandraTestEntity;
import com.excelian.mache.jmeter.cassandra.MacheAbstractCassandraKafkaSamplerClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

/**
 * JMeter test that measures writing directly to Mache.
 */
public class WriteToCache extends MacheAbstractCassandraKafkaSamplerClient {
    private static final long serialVersionUID = 2401450622395824431L;

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            writeDocumentIntoCacheWithNewData(extractParameters(context));
            result.sampleEnd();
            result.setSuccessful(true);
            result.setResponseOK();
        } catch (Exception e) {
            result.setResponseMessage(e.toString());
            result.sampleEnd();
            result.setSuccessful(false);
            getLogger().error("Error running test", e);
            result.setResponseMessage(e.toString());
        }

        return result;
    }

    private void writeDocumentIntoCacheWithNewData(final Map<String, String> params) throws Exception {
        final String docNumber = params.get("entity.keyNo");
        final String entityValue = params.get("entity.value");
        final String key = "document_" + docNumber;
        final String value = entityValue != null && entityValue.equals("CURRENTTIME")
                ? key + "_" + System.currentTimeMillis() : entityValue;

        if (cache1 == null) {
            throw new Exception("Cache object is not initialised");
        }

        cache1.put(key, new CassandraTestEntity(key, value));
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();

        defaultParameters.addArgument("entity.value", "CURRENTTIME");
        return defaultParameters;
    }
}
