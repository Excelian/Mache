package org.mache.jmeter.mongo;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class MongoWriteThrough extends MacheAbstractMongoSamplerClient {
	private static final long serialVersionUID = 3550175542777320608L;

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		final SampleResult result = new SampleResult();

		result.sampleStart();

		initMongoEntity();
		cache1.put(e.pkString, e);

		result.sampleEnd();
		result.setSuccessful(true);
		result.setResponseMessage("Put value (" + e.description
				+ ") from Cache");

		return result;
	}
}
