package org.mache.jmeter.mongo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Date;
import java.util.Map;

public class MongoReadThrough extends MacheAbstractMongoSamplerClient {
    private static final long serialVersionUID = 3550175542777320608L;

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        Map<String, String> mapParams = ExtractParameters(context);
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
                return super.SetupResultForError(result, e1);
            }
        }
        while (new Date().getTime() - startTime <= timeoutMs && (readEntity == null || !e.description.equals(readEntity.description)));

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
