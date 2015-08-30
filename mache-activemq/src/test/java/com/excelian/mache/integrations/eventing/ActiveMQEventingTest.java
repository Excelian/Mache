package com.excelian.mache.integrations.eventing;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.ActiveMQFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.concurrent.TimeUnit;

public class ActiveMQEventingTest extends TestEventingBase {

    @Override
    protected MQFactory<String> buildMQFactory() throws JMSException {
        return new ActiveMQFactory<>(new ActiveMQConnectionFactory("vm://localhost"), TimeUnit.MINUTES.toMillis(1),
                DeliveryMode.NON_PERSISTENT, Session.AUTO_ACKNOWLEDGE);
    }
}
