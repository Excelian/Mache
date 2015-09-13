package com.excelian.mache.jmeter.mongo.knownKeys;

import com.excelian.mache.jmeter.mongo.MacheAbstractMongoSamplerClient;
import com.excelian.mache.jmeter.mongo.MongoTestEntity;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Created by jbowkett on 11/09/2015.
 */
public class MongoWriteToCache extends MacheAbstractMongoSamplerClient {

    private ShuffledSequence shuffledSequence = new ShuffledSequence();

    private static final long serialVersionUID = 3550175542777320608L;

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        writeOneThousandDocumentsIntoCacheWithNewData();
        result.sampleEnd();
        result.setSuccessful(true);
        result.setResponseOK();
        return result;
    }

    private void writeOneThousandDocumentsIntoCacheWithNewData() {
        for (int i : shuffledSequence.upTo(1000)) {
            final String key = "document_" + i;
            final String value = key + "_" + System.currentTimeMillis();
            cache1.put(key, new MongoTestEntity(key, value));
        }
    }
}

