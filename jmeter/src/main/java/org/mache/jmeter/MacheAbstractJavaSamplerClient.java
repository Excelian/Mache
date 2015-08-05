package org.mache.jmeter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

@SuppressWarnings("serial")
abstract public class MacheAbstractJavaSamplerClient  extends AbstractJavaSamplerClient implements Serializable {
	protected Map<String, String> mapParams = null;
	
    protected Map<String, String> ExtractParameters(JavaSamplerContext context) {
    	mapParams = new ConcurrentHashMap<String, String>();
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
