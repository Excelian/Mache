package org.mache.integrations.eventing;

import java.io.IOException;

import javax.jms.JMSException;

import org.junit.Rule;
import org.mache.NotRunningInExcelian;
import org.mache.RabbitMQForTestsPresent;
import org.mache.events.MQFactory;
import org.mache.events.integration.RabbitMQFactory;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;

@IgnoreIf(condition = RabbitMQForTestsPresent.class)
public class RabbitMQEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new RabbitMQFactory(RabbitMQForTestsPresent.HostName());
    }
}
