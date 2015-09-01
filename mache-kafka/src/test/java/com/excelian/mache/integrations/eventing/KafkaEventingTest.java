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

@IgnoreIf(condition = NoRunningKafkaForTests.class)
public class KafkaEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

	@Override
    protected MQFactory<String> buildMQFactory() throws JMSException, IOException {
        return new KafkaMQFactory<>(new NoRunningKafkaForTests().getHost(), new DefaultKafkaMqConfig());
    }
}

