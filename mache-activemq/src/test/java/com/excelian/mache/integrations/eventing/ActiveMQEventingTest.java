package com.excelian.mache.integrations.eventing;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.ActiveMQFactory;

import javax.jms.JMSException;

public class ActiveMQEventingTest extends TestEventingBase {

    @Override
    protected MQFactory buildMQFactory() throws JMSException {
        return new ActiveMQFactory("vm://localhost");
    }
}
