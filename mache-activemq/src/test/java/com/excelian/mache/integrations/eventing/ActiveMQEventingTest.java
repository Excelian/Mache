package com.excelian.mache.integrations.eventing;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.events.integration.DefaultActiveMqConfig;

import javax.jms.JMSException;

public class ActiveMQEventingTest extends TestEventingBase {

    @Override
    protected MQFactory<String> buildMQFactory() throws JMSException {
        return new ActiveMQFactory<>("vm://localhost", new DefaultActiveMqConfig());
    }
}
