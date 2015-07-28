package org.mache.jmeter.mongo;

import java.util.Date;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class MongoReadThrough extends MacheAbstractMongoSamplerClient {
	private static final long serialVersionUID = 3550175542777320608L;

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		int sleepMilis = Integer.parseInt(mapParams.get("read.sleepMs"));
		int timeoutMs = Integer.parseInt(mapParams.get("read.timeoutMs"));

		final SampleResult result = new SampleResult();
        result.sampleStart();
        
        final long startTime = new Date().getTime();
        MongoTestEntity readEntity = null;
        
        do {
        	initMongoEntity();
            readEntity = cache1.get(e.pkString);
			try {
				Thread.sleep(sleepMilis);
			} catch (InterruptedException e1) {
				getLogger().error("Error while reading value through cache from mongo " + e1.getMessage(), e1);
				result.sampleEnd();
				result.setSuccessful(false);
				result.setResponseMessage("Error while reading value through cache from mongo " + e1.getMessage());
				return result;
			}
        } while (new Date().getTime() - startTime <=  timeoutMs && (readEntity == null || !e.description.equals(readEntity.description)));
        
        result.sampleEnd();
        result.setSuccessful(true);
    	result.setResponseMessage("Put value (" + e.description + ") from Cache");

        return result;
	}
	
    @Override
    public Arguments getDefaultParameters() {
    	final Arguments result = super.getDefaultParameters();
    	result.addArgument("read.timeoutMs", "50");
    	result.addArgument("read.sleepMs", "1");
        return result;
    }

}
