package org.mache.jmeter;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

abstract public class MacheAbstractJavaSamplerClient  extends AbstractJavaSamplerClient implements Serializable {

    static protected Map<String, String> ExtractParameters(JavaSamplerContext context) {
        Map<String, String> mapParams = new HashMap<String, String>();
        for (Iterator<String> it = context.getParameterNamesIterator(); it.hasNext();) {
            String paramName =  it.next();
            String paramValue = context.getParameter(paramName);
            mapParams.put(paramName, paramValue);
        }
        return mapParams;
    }

    protected void SetupResultForError(SampleResult result, Exception e) {
        e.printStackTrace();
        System.out.println(e);

        result.sampleEnd();
        result.setSuccessful(false);
        result.setResponseMessage("Exception: " + e);
    }
}
