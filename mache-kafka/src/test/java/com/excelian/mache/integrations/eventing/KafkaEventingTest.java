package com.excelian.mache.integrations.eventing;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.KafkaMQFactory;
import org.junit.Rule;

import javax.jms.JMSException;
import java.io.IOException;

import static com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import static com.excelian.mache.events.integration.KafkaMqConfig.KafkaMqConfigBuilder.builder;

@IgnoreIf(condition = NoRunningKafkaForTests.class)
public class KafkaEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

	@Override
    protected MQFactory<String> buildMQFactory() throws JMSException, IOException {
        return new KafkaMQFactory<>(builder().setZkHost(new NoRunningKafkaForTests().getHost()).build());
    }
}

