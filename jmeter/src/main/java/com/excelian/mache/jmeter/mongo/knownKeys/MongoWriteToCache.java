package com.excelian.mache.jmeter.mongo.knownKeys;

import com.excelian.mache.jmeter.mongo.MacheAbstractMongoSamplerClient;
import com.excelian.mache.jmeter.mongo.MongoTestEntity;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Created by jbowkett on 11/09/2015.
 */
public class MongoWriteToCache extends MacheAbstractMongoSamplerClient {


    private static final long serialVersionUID = 3550175542777320608L;

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        writeOneThousandDocumentsWithNewData();
        result.sampleEnd();
        result.setSuccessful(true);
        return result;
    }

    private void writeOneThousandDocumentsWithNewData() {
        for (int i = 0; i < 1000; i++) {
            //write a document & update it to System.currentTimeMillis()
            final String key = "document_" + i;
            final String value = key + "_" + System.currentTimeMillis();
            cache1.put(key, new MongoTestEntity(key, value));
        }
    }
}

