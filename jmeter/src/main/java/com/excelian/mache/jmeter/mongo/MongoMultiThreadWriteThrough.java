package com.excelian.mache.jmeter.mongo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import com.fasterxml.uuid.Generators;

import java.util.Map;

public class MongoMultiThreadWriteThrough extends
		MacheAbstractMongoSamplerClient {
	private static final long serialVersionUID = 3550175542777320608L;

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		Map<String, String> mapParams = extractParameters(context);
		final SampleResult result = new SampleResult();

		result.sampleStart();

		try {

			MongoTestEntity e = initMongoEntity(mapParams);
			cache1.put(e.pkString, e);
			JMeterUtils.setProperty(mapParams.get("entity.value.name"), e.description);

			result.sampleEnd();
			result.setSuccessful(true);
			result.setResponseMessage("Put value (" + e.description
					+ ") from Cache");
		} catch (Exception e1) {
			return super.setupResultForError(result, e1);
		}

		return result;
	}

	@Override
	protected MongoTestEntity initMongoEntity(Map<String, String> mapParams) {
		return new MongoTestEntity(mapParams.get("entity.key"),
				Generators.timeBasedGenerator().generate().toString());
	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = super.getDefaultParameters();
		defaultParameters.addArgument("entity.value.name", "mongo.entity.value");
		return defaultParameters;
	}
}
