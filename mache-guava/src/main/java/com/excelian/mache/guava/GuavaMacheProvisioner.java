package com.excelian.mache.guava;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provisions Guava cache for Mache.
 *
 * @param <K> the type of key to store in Mache.
 * @param <V> the type of value to store in Mache.
 */
public class GuavaMacheProvisioner<K, V> implements CacheProvisioner<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(GuavaMacheProvisioner.class);
    private static final String DEFAULT_SPEC =
            "maximumSize=10000,"
                    + "softValues,"
                    + "expireAfterWrite=1d,"
                    + "expireAfterAccess=1d,"
                    + "recordStats";
    private final CacheBuilder<K, V> builder;

    private GuavaMacheProvisioner(CacheBuilder<K, V> builder) {
        this.builder = builder;
    }

    /**
     * Provisions a Guava Mache with the default spec.
     *
     * @param <K> the type of key to store in Mache.
     * @param <V> the type of value to store in Mache.
     * @return A provisioner that uses the default guava spec to build a cache.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> GuavaMacheProvisioner<K, V> guava() {
        return new GuavaMacheProvisioner<>((CacheBuilder<K, V>) CacheBuilder.from(DEFAULT_SPEC));
    }

    /**
     * Provisions a Caffeine Mache with the provided Guava spec.
     *
     * @param spec The guava spec string to build.
     * @param <K> the type of key to store in Mache.
     * @param <V> the type of value to store in Mache.
     * @return A provisioner that uses the guava spec to build a cache.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> GuavaMacheProvisioner<K, V> guava(String spec) {
        CacheBuilder<Object, Object> builder = CacheBuilder.from(spec);
        checkWeakKeys(builder);
        return new GuavaMacheProvisioner<>((CacheBuilder<K, V>) builder);
    }

    /**
     * Provisions a Caffeine Mache with the provided Guava spec.
     *
     * @param builder The Guava builder to use.
     * @param <K> the type of key to store in Mache.
     * @param <V> the type of value to store in Mache.
     * @return A provisioner that uses the Guava builder to build a cache.
     */
    public static <K, V> GuavaMacheProvisioner guava(CacheBuilder<K, V> builder) {
        checkWeakKeys(builder);
        return new GuavaMacheProvisioner<>(builder);
    }

    private static <K, V> void checkWeakKeys(CacheBuilder<K, V> builder) {
        // weak keys cause invalidation to fail because of using identity function for equivalence see
        // http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilder.html#weakKeys()
        if (builder.toString().contains("keyString=weak")) {
            LOG.warn("Using weakKeys will cause distributed invalidation to fail.");
        }
    }

    public Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V> cacheLoader) {
        LoadingCache<K, V> cache = builder.build(new CacheLoaderAdapter<>(cacheLoader));
        return new GuavaMache<>(cacheLoader, cache);
    }

}
