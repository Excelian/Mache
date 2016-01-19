package com.excelian.mache.factory;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.builder.MessagingProvisioner;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.Mache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.excelian.mache.builder.MacheBuilder.mache;

/**
 * Factory responsible for dispensing new mache instances from a given configuration
 */
public class MacheFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MacheFactory.class);

    private final StorageProvisioner storageProvisioner;
    private final CacheProvisioner cacheProvisioner;
    private final MessagingProvisioner messagingProvisioner;

    /**
     * Creates a mache factory for the given storage/cache/messaging provisioners
     */
    public MacheFactory(CacheProvisioner cacheProvisioner,
                        StorageProvisioner storageProvisioner,
                        MessagingProvisioner messagingProvisioner) {
        this.storageProvisioner = storageProvisioner;
        this.cacheProvisioner = cacheProvisioner;
        this.messagingProvisioner = messagingProvisioner;
    }

    public <K, V> Mache<K, V> create(Class<K> keyType, Class<V> valueType) throws Exception {
        LOG.trace("Creating map for key {} value {}", keyType, valueType);
        Mache<K, V> newMache = mache(keyType, valueType)
                .cachedBy(cacheProvisioner)
                .storedIn(storageProvisioner)
                .withMessaging(messagingProvisioner)
                .macheUp();
        return newMache;
    }
}
