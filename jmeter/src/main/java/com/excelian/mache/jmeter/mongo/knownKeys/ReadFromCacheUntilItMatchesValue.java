package com.excelian.mache.jmeter.mongo.knownKeys;

import com.excelian.mache.jmeter.mongo.MongoTestEntity;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.Map;

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
            MongoTestEntity result = cache1.get(key);
            if (result.description.equals(expectedValue)) {
                //	getLogger().info("# Read k="+key+" v="+expectedValue);
                break;
            }
            if ((System.currentTimeMillis() - time) > MINUTES.toMillis(2))
            //We don't use a future as we want to stay in same thread.
            {
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
