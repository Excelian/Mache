package com.excelian.mache.jmeter.cassandra.knownKeys;

import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.excelian.mache.jmeter.cassandra.CassandraTestEntity;
import com.excelian.mache.jmeter.cassandra.MacheAbstractCassandraKafkaSamplerClient;

public class WriteToCache extends MacheAbstractCassandraKafkaSamplerClient {
	private static final long serialVersionUID = 2401450622395824431L;

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		final SampleResult result = new SampleResult();
		result.sampleStart();
		writeDocumentIntoCacheWithNewData(extractParameters(context));
		result.sampleEnd();
		result.setSuccessful(true);
		result.setResponseOK();
		return result;
	}

	private void writeDocumentIntoCacheWithNewData(final Map<String, String> params) {
		final String docNumber = params.get("entity.keyNo");
		final String key = "document_" + docNumber;
		final String value = key + "_" + System.currentTimeMillis();
		cache1.put(key, new CassandraTestEntity(key, value));
	}
}
