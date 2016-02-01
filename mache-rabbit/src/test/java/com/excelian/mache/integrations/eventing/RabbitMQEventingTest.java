package com.excelian.mache.integrations.eventing;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.RabbitMQFactory;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Rule;

import javax.jms.JMSException;
import java.io.IOException;

import static com.excelian.mache.events.integration.RabbitMQConfig.RabbitMQConfigBuilder.builder;

@IgnoreIf(condition = NoRunningRabbitMQForTests.class)
public class RabbitMQEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private static RabbitMQFactory<String> mqFactory;

    @Override
    protected MQFactory<String> buildMQFactory() throws JMSException, IOException {
        if (mqFactory == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(new NoRunningRabbitMQForTests().getHost());
            factory.setAutomaticRecoveryEnabled(true);
            mqFactory = new RabbitMQFactory<>(factory, builder().build());
        }

        return mqFactory;
    }

    @Override
    public void afterEachTestcase() throws IOException {
        if (theProducer != null) {
            theProducer.close();
        }
        if (theConsumer != null) {
            theConsumer.close();
        }
    }
}
