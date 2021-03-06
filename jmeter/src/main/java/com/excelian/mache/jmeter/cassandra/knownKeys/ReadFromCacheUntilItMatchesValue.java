package com.excelian.mache.jmeter.cassandra.knownkeys;

import com.excelian.mache.jmeter.cassandra.CassandraTestEntity;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * JMeter test that measures reading directly from Mache until it matches a particular value.
 */
public class ReadFromCacheUntilItMatchesValue extends ReadFromCache {
    private static final long serialVersionUID = -8612323545680365704L;

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
        return super.runTest(context);
    }

    @Override
    protected void readFromCache(final Map<String, String> params) throws Exception {
        final String docNumber = params.get("entity.keyNo");
        final String key = "document_" + docNumber;
        final String expectedValue = params.get("entity.expectedValue");

        Long time = System.currentTimeMillis();

        do {
            CassandraTestEntity result = cache1.get(key);
            if (result.aString.equals(expectedValue)) {
                //getLogger().info("# Read k="+key+" v="+expectedValue);
                break;
            }
            if ((System.currentTimeMillis() - time) > MINUTES.toMillis(2)) {
                //We don't use a future as we want to stay in same thread.
                throw new Exception("It took too long to observe cache value change k=" + key + " v=" + expectedValue);
            }
        } while (true);
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();

        defaultParameters.addArgument("entity.expectedValue", "ExpectedValue");
        return defaultParameters;
    }
}
