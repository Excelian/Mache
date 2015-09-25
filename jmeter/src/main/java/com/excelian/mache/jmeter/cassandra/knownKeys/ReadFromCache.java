package com.excelian.mache.jmeter.cassandra.knownKeys;

import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.excelian.mache.jmeter.cassandra.MacheAbstractCassandraKafkaSamplerClient;

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
			getLogger().error("Error connecting to cache", e);
		}

		return result;
	}

	private void readFromCache(final Map<String, String> params) {
		final String docNumber = params.get("entity.keyNo");
		final String key = "document_" + docNumber;

		getLogger().info("Loading " + key + " cache is " + cache1);

		cache1.get(key);
	}
}
