package com.excelian.mache.integrations.eventing;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.excelian.mache.core.NoRunningRabbitMQForTests;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.DefaultRabbitMqConfig;
import com.excelian.mache.events.integration.RabbitMQFactory;
import org.junit.Rule;

import javax.jms.JMSException;
import java.io.IOException;

@IgnoreIf(condition = NoRunningRabbitMQForTests.class)
public class RabbitMQEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new RabbitMQFactory(new NoRunningRabbitMQForTests().getHost(), new DefaultRabbitMqConfig());
    }
}
