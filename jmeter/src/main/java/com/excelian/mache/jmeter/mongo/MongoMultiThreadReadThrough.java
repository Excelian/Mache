package com.excelian.mache.jmeter.mongo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import com.fasterxml.uuid.Generators;

import java.util.Date;
import java.util.Map;

public class MongoMultiThreadReadThrough extends MacheAbstractMongoSamplerClient {
    private static final long serialVersionUID = 3550175542777320608L;

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        Map<String, String> mapParams = extractParameters(context);
        int sleepMilis = Integer.parseInt(mapParams.get("read.sleepMs"));
        int timeoutMs = Integer.parseInt(mapParams.get("read.timeoutMs"));

        final SampleResult result = new SampleResult();
        result.sampleStart();

        final long startTime = new Date().getTime();
        MongoTestEntity readEntity = null;
        MongoTestEntity e = initMongoEntity(mapParams);

        do {
            readEntity = cache1.get(e.pkString);
            try {
                Thread.sleep(sleepMilis);
            } catch (InterruptedException e1) {
                getLogger().error("Error while reading value through cache from mongo " + e1.getMessage(), e1);
                return super.setupResultForError(result, e1);
            }
        }
        while (new Date().getTime() - startTime <= timeoutMs && (readEntity == null || !e.description.equals(readEntity.description)));

        result.sampleEnd();
        result.setSuccessful(true);
        result.setResponseMessage("Put value (" + e.description + ") from Cache");

        return result;
    }

	@Override
	protected MongoTestEntity initMongoEntity(Map<String, String> mapParams) {
		return new MongoTestEntity(mapParams.get("entity.key"),
				JMeterUtils.getProperty(mapParams.get("entity.value.name")));
	}
	
    @Override
    public Arguments getDefaultParameters() {
        final Arguments result = super.getDefaultParameters();
        result.addArgument("read.timeoutMs", "100");
        result.addArgument("read.sleepMs", "0");
        result.addArgument("entity.value.name", "mongo.entity.value");
        return result;
    }
}
