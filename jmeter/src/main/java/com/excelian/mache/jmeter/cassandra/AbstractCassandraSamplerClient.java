package com.excelian.mache.jmeter.cassandra;

import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

/**
 * Provides base client for Cassandra JMeter tests.
 */
@SuppressWarnings("serial")
public abstract class AbstractCassandraSamplerClient extends MacheAbstractJavaSamplerClient {
    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("mache setupTest started  \n");
        //extractParameters(context);
        getLogger().info("mache setupTest completed. ");
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("cassandra.server.ip.address", "192.168.1.18");
        defaultParameters.addArgument("keyspace.name", "JMeterReadThrough");
        defaultParameters.addArgument("entity.keyNo", "${randomKeyNumber}");
        return defaultParameters;
    }
}
