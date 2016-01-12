package com.excelian.mache.jmeter.cassandra.knownkeys;

import com.excelian.mache.jmeter.cassandra.MacheAbstractCassandraKafkaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

/**
 * JMeter test that measures reading directly from Mache.
 */
public class ReadFromCache extends MacheAbstractCassandraKafkaSamplerClient {
    private static final long serialVersionUID = -8612414345680365704L;

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
            readFromCache(extractParameters(context));
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

    protected void readFromCache(final Map<String, String> params) throws Exception {
        final String docNumber = params.get("entity.keyNo");
        final String key = "document_" + docNumber;
        //getLogger().info("Loading " + key + " cache is " + cache1);
        cache1.get(key);
    }
}
