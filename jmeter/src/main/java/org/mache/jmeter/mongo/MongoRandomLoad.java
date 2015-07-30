package org.mache.jmeter.mongo;

import java.util.Random;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class MongoRandomLoad extends MacheAbstractMongoSamplerClient {
	private static final long serialVersionUID = -6670630256079770024L;
    final Random r = new Random();

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		final SampleResult result = new SampleResult();
        result.sampleStart();
        
        initMongoEntity();
		
        final String cache1Value = String.valueOf(r.nextInt());

        getLogger().debug("Putting new random values " + cache1Value);

        cache1.put(e.pkString, new MongoTestEntity(e.pkString, cache1Value));
		try {
			Thread.sleep(Long.parseLong(mapParams.get("write.sleepMs")));
		} catch (InterruptedException e1) {
			getLogger().error("Put value (" + e.description
					+ ") from Cache - error occured " +e1.getMessage(), e1);
			result.sampleEnd();
			result.setSuccessful(false);
			result.setResponseMessage("Put value (" + e.description
					+ ") from Cache - error occured " +e1.getMessage());

			return result;
		}

		result.sampleEnd();
		result.setSuccessful(true);
		result.setResponseMessage("Put value (" + e.description
				+ ") from Cache");

		return result;
	}

	@Override
	public Arguments getDefaultParameters() {
		final Arguments result = super.getDefaultParameters();

		result.addArgument("write.sleepMs", "20");
		return result;
	}
}