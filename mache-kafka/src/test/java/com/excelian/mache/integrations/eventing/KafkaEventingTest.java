package com.excelian.mache.integrations.eventing;

import com.codeaffine.test.ConditionalIgnoreRule;

import com.excelian.mache.core.NoRunningKafkaForTests;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.DefaultKafkaMqConfig;
import com.excelian.mache.events.integration.KafkaMQFactory;
import org.junit.Rule;
import com.excelian.mache.core.NotRunningInExcelian;

import javax.jms.JMSException;

import java.io.IOException;

import static com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;

@IgnoreIf(condition = NotRunningInExcelian.class)
public class KafkaEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @SuppressWarnings("rawtypes")
	@Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new KafkaMQFactory(new NoRunningKafkaForTests().HostName(), new DefaultKafkaMqConfig());
    }
}

