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

public abstract class AbstractMessagingProvisioner implements MessagingProvisioner {

    protected final String topic;

    protected AbstractMessagingProvisioner(String topic) {
        this.topic = topic;
    }

    @Override
    public <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn) throws Exception {

        final MQFactory<K> mqFactory = getMqFactory();
        final MQConfiguration mqConfiguration = () -> topic;

        final MessageQueueObservableCacheFactory<K, V> cacheFactory =
                new MessageQueueObservableCacheFactory<>(mqFactory, mqConfiguration, new UUIDUtils());

        return cacheFactory.createCache(toWireIn);
    }

    public abstract <K> MQFactory<K> getMqFactory() throws IOException, JMSException;

}
