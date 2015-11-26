package com.excelian.mache.guava;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuavaMacheProvisioner<K, V>  implements CacheProvisioner<K, V>  {

    private static final Logger LOG = LoggerFactory.getLogger(GuavaMacheProvisioner.class);
    private final CacheBuilder<K, V> builder;

    private GuavaMacheProvisioner(CacheBuilder<K, V> builder) {
        this.builder = builder;
    }

    public Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V, ?> cacheLoader) {
        LoadingCache<K, V> cache = builder.build(new CacheLoaderAdapter<>(cacheLoader));
        return new GuavaMache<>(cacheLoader, cache);
    }

    private static final String DEFAULT_SPEC =
            "maximumSize=10000,"
            + "softValues,"
            + "expireAfterWrite=1d,"
            + "expireAfterAccess=1d,"
            + "recordStats";

    @SuppressWarnings("unchecked")
    public static <K, V> GuavaMacheProvisioner<K, V> guava() {
        return new GuavaMacheProvisioner<>((CacheBuilder<K, V>) CacheBuilder.from(DEFAULT_SPEC));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> GuavaMacheProvisioner<K, V> guava(String spec) {
        CacheBuilder<Object, Object> builder = CacheBuilder.from(spec);
        checkWeakKeys(builder);
        return new GuavaMacheProvisioner<>((CacheBuilder<K, V>) builder);
    }

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

}
