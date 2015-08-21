package com.excelian.mache.observable.builder;

import com.excelian.mache.builder.MessagingProvisioner;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.observable.MessageQueueObservableCacheFactory;
import com.excelian.mache.observable.utils.UUIDUtils;

import java.io.IOException;
import javax.jms.JMSException;

/**
 * Created by jbowkett on 21/08/2015.
 */
public abstract class AbstractMessagingProvisioner implements MessagingProvisioner {

    @Override
    public <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn, String topic, String messagingLocation) throws Exception {
        final MQFactory mqFactory = getMqFactory(messagingLocation);
        final MQConfiguration mqConfiguration = () -> topic;

        final MacheFactory macheFactory = new MacheFactory();

        final MessageQueueObservableCacheFactory cacheFactory = new MessageQueueObservableCacheFactory(
            mqFactory, mqConfiguration, macheFactory, new UUIDUtils());
        return cacheFactory.createCache(toWireIn);
    }

    public abstract MQFactory getMqFactory(String messagingLocation) throws IOException, JMSException;
}
