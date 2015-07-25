package org.mache;

import com.fasterxml.uuid.Generators;
import com.google.common.cache.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;


public class CacheThing<K,V> implements ExCache<K,V>  {

    private final ForwardingCache<K, V> fwdCache;
    final private ExCacheLoader cacheLoader;
    private final UUID cacheId;

    //XXX weak keys cause invalidation tto fail because of using identity function for quivalence see http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilder.html#weakKeys()
    //private volatile String spec = "maximumSize=10000,weakKeys,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";
    private volatile String spec = "maximumSize=10000,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";

    private final LoadingCache<K, V> cache;
    private volatile boolean created;

    public CacheThing(final ExCacheLoader cacheLoader, String... optionalSpec) {
        this.cacheLoader = cacheLoader;
        if (optionalSpec != null && optionalSpec.length > 0) this.spec = optionalSpec[0];

        cache = CacheBuilder.from(spec).
                recordStats()/*.removalListener((RemovalListener<K,V>) cacheLoader)*/.
                build((CacheLoader<K,V>) cacheLoader);

        fwdCache = new ForwardingCache<K, V>() {
            @Override
            protected Cache<K, V> delegate() {
                return cache;
            }

            @Override
            public void put(K key, V value) {
                delegate().put(key, value);
            }

            @Override
            public void invalidate(Object key) {
                delegate().invalidate(key);
            }
        };

        cacheId = Generators.nameBasedGenerator().generate(getName() + String.valueOf(this));
    }

    @Override
    public String getName() {
        return cacheLoader.getName();
    }

    @Override
    public UUID getId() { return cacheId; }

    @Override
    public V get(final K k) {
        createMaybe(k);
        // a bit crappy - but the fwdrCache doesnt expose 'getOrLoad(K, Loader)'

        if (cache.asMap().containsKey(k)) {
            final ConcurrentMap<K, V> cacheMap = cache.asMap();
            System.out.println("TH " + Thread.currentThread() + " Loading from cache " + this + " id=" + getId() + " using key \"" + k + "\" [cached] - result " + cacheMap.get(k));
        }

        final V result = cache.getUnchecked(k);
        System.out.println("TH " + Thread.currentThread() + " Loading from cache " + this + " id=" + getId() + " using key \"" + k + "\" - result " + result);
        return result;
    }

    @Override
    public void put(K k, V v) {

        createMaybe(k);
        fwdCache.invalidate(k);
        cacheLoader.put(k, v);
    }
    @Override
    public void remove(K k) {
        createMaybe(k);
        cacheLoader.remove(k);
        fwdCache.invalidate(k);
    }
    @Override
    public void invalidateAll() {
        fwdCache.invalidateAll();

    }

    @Override
    public void invalidate(K k) {
        System.out.println("TH " + Thread.currentThread() + " [" + this + "] Invalidating cache id=" + getId() + " invalidated key=\"" + k + "\" (hashcode=" + k.hashCode() + ") from " + fwdCache.asMap());
        fwdCache.invalidate(k);

        if (fwdCache.asMap().containsKey(k)) {
            System.out.println("TH " + Thread.currentThread() + " [" + this + "] After invalidating key=" + k + " is still there.");
        }

        System.out.println("TH " + Thread.currentThread() + " [" + this + "] After invalidating cache is " + fwdCache.asMap());

        System.out.println("TH " + Thread.currentThread() + " [" + this + "] Keyset in cache: ");
        for (final K key: fwdCache.asMap().keySet()) {
            System.out.println("TH " + Thread.currentThread() + " [" + this + "] \"" + key + "\" (hC=" + key.hashCode() + ")");
        }
    }

    private void createMaybe(K k) {
        if (!created) {
            synchronized (this) {
                if (!created) {
                    cacheLoader.create(cacheLoader.getName(), k);
                    created = true;
                }
            }
        }
    }

    @Override
    public void close() {
        cacheLoader.close();
    }

    @Override
    public ExCacheLoader getCacheLoader() {
        return cacheLoader;
    }
}
