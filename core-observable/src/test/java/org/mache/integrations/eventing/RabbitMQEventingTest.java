package org.mache.integrations.eventing;

import java.io.IOException;

import javax.jms.JMSException;

import org.junit.Rule;
import org.mache.NoRunningRabbitMQForTests;
import org.mache.events.MQFactory;
import org.mache.events.integration.RabbitMQFactory;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;

@IgnoreIf(condition = NoRunningRabbitMQForTests.class)
public class RabbitMQEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new RabbitMQFactory(NoRunningRabbitMQForTests.HostName());
    }
}
