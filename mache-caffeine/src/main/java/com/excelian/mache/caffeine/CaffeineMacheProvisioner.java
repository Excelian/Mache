package com.excelian.mache.caffeine;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Provisions Caffeine cache for Mache.
 *
 * @param <K> the type of key to store in Mache.
 * @param <V> the type of value to store in Mache.
 */
public class CaffeineMacheProvisioner<K, V> implements CacheProvisioner<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(CaffeineMacheProvisioner.class);
    private final Caffeine<K, V> builder;

    private CaffeineMacheProvisioner(Caffeine<K, V> builder) {
        this.builder = builder;
    }

    public Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V> cacheLoader) {
        LoadingCache<K, V> cache = builder.build(new CacheLoaderAdapter<>(cacheLoader));
        return new CaffeineMache<>(cacheLoader, cache);
    }

    /**
     * Provisions a Caffeine Mache with the default Caffeine values.
     *
     * @param <K> the type of key to store in Mache.
     * @param <V> the type of value to store in Mache.
     * @return a provisioner that uses the default values to create a cache.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> CaffeineMacheProvisioner<K, V> caffeine() {
        return new CaffeineMacheProvisioner<>((Caffeine<K, V>) Caffeine.newBuilder()
                .maximumSize(10000)
                .softValues()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .expireAfterAccess(1, TimeUnit.DAYS));
    }

    /**
     * Provisions a Caffeine Mache with the provided Caffeine builder.
     *
     * @param caffeine The builder to create the Caffeine cache with.
     * @param <K> the type of key to store in Mache.
     * @param <V> the type of value to store in Mache.
     * @return a provisioner that uses the provided builder to create a cache.
     */
    public static <K, V> CaffeineMacheProvisioner<K, V> caffeine(Caffeine<K, V> caffeine) {
        // weak keys cause invalidation to fail because of using identity function for equivalence see
        // http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilder.html#weakKeys()
        if (caffeine.toString().contains("keyStrength=weak")) {
            LOG.warn("Using weakKeys will cause distributed invalidation to fail.");
        }
        return new CaffeineMacheProvisioner<>(caffeine);
    }

    @Override
    public String toString() {
        return "CaffeineMacheProvisioner{"
                + "builder=" + builder
                + '}';
    }
}
