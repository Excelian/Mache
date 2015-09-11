package com.excelian.mache.jmeter.mongo.knownKeys;

import com.excelian.mache.jmeter.mongo.MacheAbstractMongoSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Created by jbowkett on 11/09/2015.
 */
public class MongoReadFromCache extends MacheAbstractMongoSamplerClient {


    private static final long serialVersionUID = 3550175542777320608L;

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        readOneThousandDocumentsWithNewData();
        result.sampleEnd();
        result.setSuccessful(true);
        return result;
    }

    private void readOneThousandDocumentsWithNewData() {
        for (int i = 0; i < 1000; i++) {
            //write a document & update it to System.currentTimeMillis()
            final String key = "document_" + i;
            cache1.get(key);
        }
    }
}

