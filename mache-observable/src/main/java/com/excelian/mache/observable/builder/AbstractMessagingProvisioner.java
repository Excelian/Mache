package com.excelian.mache.observable.builder;

import com.excelian.mache.builder.MessagingProvisioner;
import com.excelian.mache.core.Mache;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.observable.MessageQueueObservableCacheFactory;
import com.excelian.mache.observable.utils.UuidUtils;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Base class for provisioners of messaging.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public abstract class AbstractMessagingProvisioner<K, V> implements MessagingProvisioner<K, V> {

    protected final String topic;

    protected AbstractMessagingProvisioner(String topic) {
        this.topic = topic;
    }

    @Override
    public Mache<K, V> wireInMessaging(Mache<K, V> toWireIn) throws Exception {

        final MQFactory<K> mqFactory = getMqFactory();
        final MQConfiguration mqConfiguration = () -> topic;

        final MessageQueueObservableCacheFactory<K, V> cacheFactory =
                new MessageQueueObservableCacheFactory<>(mqFactory, mqConfiguration, new UuidUtils());

        return cacheFactory.createCache(toWireIn);
    }

    public abstract MQFactory<K> getMqFactory() throws IOException, JMSException;

}
