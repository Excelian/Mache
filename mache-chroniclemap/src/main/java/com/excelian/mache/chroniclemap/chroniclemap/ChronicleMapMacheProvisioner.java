package com.excelian.mache.chroniclemap.chroniclemap;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.chroniclemap.chroniclemap.solr.ConcurrentLRUCache;
import com.excelian.mache.chroniclemap.chroniclemap.solr.ConcurrentLRUCache.EvictionListener;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

public class ChronicleMapMacheProvisioner<K, V> implements CacheProvisioner<K, V> {

    private final int upperWaterMark;
    private final int lowerWaterMark;
    private final int acceptableWatermark;
    private final boolean runCleanupThread;
    private final boolean runNewThreadForCleanup;
    private final EvictionListener<K, V> evictionListener;
    private final ChronicleMapBuilder<K, V> chronicleMapBuilder;
    private final File persistedTo;

    public ChronicleMapMacheProvisioner(int upperWaterMark, int lowerWaterMark, int acceptableWatermark,
                                        boolean runCleanupThread, boolean runNewThreadForCleanup,
                                        EvictionListener<K, V> evictionListener,
                                        ChronicleMapBuilder<K, V> chronicleMapBuilder, File persistedTo) {
        this.upperWaterMark = upperWaterMark;
        this.lowerWaterMark = lowerWaterMark;
        this.acceptableWatermark = acceptableWatermark;
        this.runCleanupThread = runCleanupThread;
        this.runNewThreadForCleanup = runNewThreadForCleanup;
        this.evictionListener = evictionListener;
        this.chronicleMapBuilder = chronicleMapBuilder;
        this.persistedTo = persistedTo;
    }

    public static <K, V> ChronicleMapMacheProvisioner<K, V> chronicleMap() {
        return new ChronicleMapMacheBuilder<K, V>(null).create();
    }

    public static <K, V> ChronicleMapMacheBuilder<K, V> chronicleMap(ChronicleMapBuilder<K, V> builder) {
        return new ChronicleMapMacheBuilder<>(builder);
    }

    public static <K, V> ChronicleMapMacheBuilder<K, V> chronicleMap(ChronicleMapBuilder<K, V> builder, File persistedTo) {
        return new ChronicleMapMacheBuilder<>(builder, persistedTo);
    }

    @Override
    public Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V, ?> cacheLoader) {
        ConcurrentMap<K, V> map;

        if (chronicleMapBuilder == null) {
            map = ChronicleMapBuilder.of(keyType, valueType)
                    .entries(upperWaterMark + (upperWaterMark - lowerWaterMark))
                    .create();
        } else if (persistedTo == null) {
            map = chronicleMapBuilder.create();
        } else {
            try {
                map = chronicleMapBuilder.createPersistedTo(persistedTo);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to create persisted Chronicle Map.", e);
            }
        }

        ConcurrentLRUCache<K, V> cache = new ConcurrentLRUCache<>(upperWaterMark, lowerWaterMark,
                acceptableWatermark, runCleanupThread, runNewThreadForCleanup, evictionListener, map);

        return new ChronicleMapMache<>(cacheLoader, cache);
    }

    public static class ChronicleMapMacheBuilder<K, V> {

        private final ChronicleMapBuilder<K, V> chronicleMapBuilder;
        private final File persistedTo;
        private int upperWaterMark = 10000;
        private int lowerWaterMark = 8000;
        private int acceptableWatermark = 9000;
        private boolean runCleanupThread = false;
        private boolean runNewThreadForCleanup = false;
        private EvictionListener<K, V> evictionListener;

        public ChronicleMapMacheBuilder(ChronicleMapBuilder<K, V> chronicleMapBuilder) {
            this.chronicleMapBuilder = chronicleMapBuilder;
            this.persistedTo = null;
        }

        public ChronicleMapMacheBuilder(ChronicleMapBuilder<K, V> builder, File persistedTo) {
            this.chronicleMapBuilder = builder;
            this.persistedTo = persistedTo;
        }


        public ChronicleMapMacheBuilder withUpperWaterMark(int upperWaterMark) {
            this.upperWaterMark = upperWaterMark;
            return this;
        }

        public ChronicleMapMacheBuilder withLowerWaterMark(int lowerWaterMark) {
            this.lowerWaterMark = lowerWaterMark;
            return this;
        }

        public ChronicleMapMacheBuilder withAcceptableWatermark(int acceptableWatermark) {
            this.acceptableWatermark = acceptableWatermark;
            return this;
        }

        public ChronicleMapMacheBuilder withRunCleanupThread(boolean runCleanupThread) {
            this.runCleanupThread = runCleanupThread;
            return this;
        }

        public ChronicleMapMacheBuilder withRunNewThreadForCleanup(boolean runNewThreadForCleanup) {
            this.runNewThreadForCleanup = runNewThreadForCleanup;
            return this;
        }

        public ChronicleMapMacheBuilder withEvictionListener(EvictionListener<K, V> evictionListener) {
            this.evictionListener = evictionListener;
            return this;
        }

        public ChronicleMapMacheProvisioner<K, V> create() {
            return new ChronicleMapMacheProvisioner<>(upperWaterMark, lowerWaterMark, acceptableWatermark,
                    runCleanupThread, runNewThreadForCleanup, evictionListener, chronicleMapBuilder, persistedTo);
        }
    }
}