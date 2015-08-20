package org.mache.integrations.eventing;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import org.junit.Rule;
import org.mache.NoRunningRabbitMQForTests;
import org.mache.events.MQFactory;
import org.mache.events.integration.RabbitMQFactory;

import javax.jms.JMSException;
import java.io.IOException;

@IgnoreIf(condition = NoRunningRabbitMQForTests.class)
public class RabbitMQEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new RabbitMQFactory(new NoRunningRabbitMQForTests().HostName());
    }
}
