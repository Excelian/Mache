package com.excelian.mache.jmeter;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides base client for Mache JMeter tests.
 */
@SuppressWarnings("serial")
public abstract class MacheAbstractJavaSamplerClient extends AbstractJavaSamplerClient {

    private static final Logger LOG = LoggerFactory.getLogger(MacheAbstractJavaSamplerClient.class);

    protected Map<String, String> extractParameters(JavaSamplerContext context) {
        Map<String, String> mapParams = new ConcurrentHashMap<>();
        for (Iterator<String> it = context.getParameterNamesIterator(); it.hasNext(); ) {
            String paramName = it.next();
            String paramValue = context.getParameter(paramName);
            mapParams.put(paramName, paramValue);
        }
        return mapParams;
    }

    protected SampleResult setupResultForError(SampleResult result, Exception e) {
        LOG.error("Error occurred during jmeter run.", e);

        result.sampleEnd();
        result.setSuccessful(false);
        result.setResponseMessage("Exception: " + e);

        return result;
    }
}
