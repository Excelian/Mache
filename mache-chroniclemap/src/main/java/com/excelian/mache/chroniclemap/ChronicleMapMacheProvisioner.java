package com.excelian.mache.chroniclemap;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.chroniclemap.solr.ConcurrentLRUCache;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import net.openhft.chronicle.hash.ChronicleHashErrorListener;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;
import net.openhft.chronicle.hash.serialization.SizeMarshaller;
import net.openhft.chronicle.map.BytesMapEventListener;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.DefaultValueProvider;
import net.openhft.chronicle.map.MapEventListener;
import net.openhft.chronicle.map.PrepareValueBytes;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.ObjectFactory;
import net.openhft.lang.io.serialization.ObjectSerializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Provisions a {@link ChronicleMapMache} backed by the off heap store ChronicleMap.
 *
 * @param <K> key type of the cache to be provisioned.
 * @param <V> value type of the cache to be provisioned.
 */
public class ChronicleMapMacheProvisioner<K, V> implements CacheProvisioner<K, V> {

    private final int upperWaterMark;
    private final int lowerWaterMark;
    private final int acceptableWatermark;
    private final boolean runCleanupThread;
    private final boolean runNewThreadForCleanup;
    private final ConcurrentLRUCache.EvictionListener<K, V> evictionListener;
    private final File persistedTo;
    private List<ChronicleMapBuilderLambda<K, V>> configToApply;

    /**
     * @param upperWaterMark start clearing cache when size above this mark.
     * @param lowerWaterMark lower mark to attempt to clear down to.
     * @param acceptableWatermark mark where clearing is considered finished.
     * @param runCleanupThread run clean up in separate thread.
     * @param runNewThreadForCleanup run clean up in new thread each time.
     * @param evictionListener listens for evictions due to size.
     * @param persistedTo The location to persist to.
     * @param configToApply Things to apply to the builder for the backing ChronicleMap.
     */
    public ChronicleMapMacheProvisioner(int upperWaterMark, int lowerWaterMark, int acceptableWatermark,
                                        boolean runCleanupThread, boolean runNewThreadForCleanup,
                                        ConcurrentLRUCache.EvictionListener<K, V> evictionListener,
                                        File persistedTo, List<ChronicleMapBuilderLambda<K, V>> configToApply) {
        this.upperWaterMark = upperWaterMark;
        this.lowerWaterMark = lowerWaterMark;
        this.acceptableWatermark = acceptableWatermark;
        this.runCleanupThread = runCleanupThread;
        this.runNewThreadForCleanup = runNewThreadForCleanup;
        this.evictionListener = evictionListener;
        this.persistedTo = persistedTo;
        this.configToApply = configToApply;
    }

    public static <K, V> ChronicleMapMacheBuilder<K, V> chronicleMap() {
        return new ChronicleMapMacheBuilder<>();
    }

    @Override
    public Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V> cacheLoader) {
        ChronicleMapBuilder<K, V> chronicleMapBuilder = ChronicleMapBuilder.of(keyType, valueType);

        configToApply.stream().forEach((lambda) -> lambda.apply(chronicleMapBuilder));

        ConcurrentMap<K, V> map;

        if (persistedTo == null) {
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

    /**
     * Adds a call that needs to be applied to the builder when building the backing ChronicleMap.
     * @param <K> The key type of the backing Chronicle Map.
     * @param <V> The value type of the backing Chronicle Map.
     */
    @FunctionalInterface
    private interface ChronicleMapBuilderLambda<K, V> {
        ChronicleMapBuilder<K, V> apply(ChronicleMapBuilder<K, V> builder);
    }

    /**
     * Provisions a ChronicleMap Mache Builder.
     *
     * @param <K> the type of key to store in Mache.
     * @param <V> the type of value to store in Mache.
     */
    public static class ChronicleMapMacheBuilder<K, V> {
        private File persistedTo;
        private List<ChronicleMapBuilderLambda<K, V>> configToApply = new ArrayList<>();

        private int upperWaterMark = 10000;
        private int lowerWaterMark = 9000;
        private int acceptableWatermark = 9500;

        private boolean runCleanupThread = false;
        private boolean runNewThreadForCleanup = false;
        private ConcurrentLRUCache.EvictionListener<K, V> evictionListener;


        public ChronicleMapMacheProvisioner<K, V> create() {
            return new ChronicleMapMacheProvisioner<>(upperWaterMark, lowerWaterMark, acceptableWatermark,
                    runCleanupThread, runNewThreadForCleanup, evictionListener, persistedTo, configToApply);
        }

        /**
         * Sets the size for the ChronicleMap cache. Note: Due to the fact ChronicleMap will break if
         * we attempt to add more entries than specified, and the ConcurrentLRUCache will simply try
         * to restrict to around the watermarks, we add buffer to help ensure entries are removed before
         * hitting the limit. Be aware this can still fail if adding elements to the cache quickly while
         * running the CleanUpThread/running clean up in a new thread.
         *
         * @param size The desired size of the cache.
         * @return The builder being built.
         */
        public ChronicleMapMacheBuilder<K, V> size(int size) {
            this.upperWaterMark = size;
            this.acceptableWatermark = size - (int) Math.ceil(size * 0.15);
            this.lowerWaterMark = size - (int) Math.floor(size * 0.15) - 1;

            this.configToApply.add((builder -> builder.entries(size + (int) Math.ceil(size * 0.15))));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> persistingTo(File persistedTo) {
            this.persistedTo = persistedTo;
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> withRunCleanupThread(boolean runCleanupThread) {
            this.runCleanupThread = runCleanupThread;
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> withRunNewThreadForCleanup(boolean runNewThreadForCleanup) {
            this.runNewThreadForCleanup = runNewThreadForCleanup;
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> withEvictionListener(
                ConcurrentLRUCache.EvictionListener<K, V> evictionListener) {
            this.evictionListener = evictionListener;
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> averageKeySize(double averageKeySize) {
            configToApply.add((builder -> builder.averageKeySize(averageKeySize)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> constantKeySizeBySample(K sampleKey) {
            configToApply.add((builder -> builder.constantKeySizeBySample(sampleKey)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> averageValueSize(double averageValueSize) {
            configToApply.add((builder -> builder.averageValueSize(averageValueSize)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> constantValueSizeBySample(V sampleValue) {
            configToApply.add((builder -> builder.constantValueSizeBySample(sampleValue)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> actualChunkSize(int actualChunkSize) {
            configToApply.add((builder -> builder.actualChunkSize(actualChunkSize)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> maxChunksPerEntry(int maxChunksPerEntry) {
            configToApply.add((builder -> builder.maxChunksPerEntry(maxChunksPerEntry)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> entriesPerSegment(long entriesPerSegment) {
            configToApply.add((builder -> builder.entriesPerSegment(entriesPerSegment)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> actualChunksPerSegment(long actualChunksPerSegment) {
            configToApply.add((builder -> builder.actualChunksPerSegment(actualChunksPerSegment)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> minSegments(int minSegments) {
            configToApply.add((builder -> builder.minSegments(minSegments)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> actualSegments(int actualSegments) {
            configToApply.add((builder -> builder.actualSegments(actualSegments)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> lockTimeOut(long lockTimeOut, TimeUnit timeUnit) {
            configToApply.add((builder -> builder.lockTimeOut(lockTimeOut, timeUnit)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> errorListener(ChronicleHashErrorListener errorListener) {
            configToApply.add((builder -> builder.errorListener(errorListener)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> putReturnsNull(boolean putReturnsNull) {
            configToApply.add((builder -> builder.putReturnsNull(putReturnsNull)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> removeReturnsNull(boolean removeReturnsNull) {
            configToApply.add((builder -> builder.removeReturnsNull(removeReturnsNull)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> bytesMarshallerFactory(BytesMarshallerFactory bytesMarshallerFactory) {
            configToApply.add((builder -> builder.bytesMarshallerFactory(bytesMarshallerFactory)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> objectSerializer(ObjectSerializer objectSerializer) {
            configToApply.add((builder -> builder.objectSerializer(objectSerializer)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> keyMarshaller(BytesMarshaller<? super K> keyMarshaller) {
            configToApply.add((builder -> builder.keyMarshaller(keyMarshaller)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> keyMarshallers(BytesWriter<K> keyWriter, BytesReader<K> keyReader) {
            configToApply.add((builder -> builder.keyMarshallers(keyWriter, keyReader)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> keySizeMarshaller(SizeMarshaller keySizeMarshaller) {
            configToApply.add((builder -> builder.keySizeMarshaller(keySizeMarshaller)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> keyDeserializationFactory(ObjectFactory<K> keyDeserializationFactory) {
            configToApply.add((builder -> builder.keyDeserializationFactory(keyDeserializationFactory)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> immutableKeys() {
            configToApply.add((ChronicleMapBuilder::immutableKeys));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> valueMarshaller(BytesMarshaller<? super V> valueMarshaller) {
            configToApply.add((builder -> builder.valueMarshaller(valueMarshaller)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> valueMarshallers(BytesWriter<V> valueWriter, BytesReader<V> valueReader) {
            configToApply.add((builder -> builder.valueMarshallers(valueWriter, valueReader)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> valueSizeMarshaller(SizeMarshaller valueSizeMarshaller) {
            configToApply.add((builder -> builder.valueSizeMarshaller(valueSizeMarshaller)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> valueDeserializationFactory(ObjectFactory<V> valueDeserializationFactory) {
            configToApply.add((builder -> builder.valueDeserializationFactory(valueDeserializationFactory)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> eventListener(MapEventListener<K, V> eventListener) {
            configToApply.add((builder -> builder.eventListener(eventListener)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> bytesEventListener(BytesMapEventListener bytesEventListener) {
            configToApply.add((builder -> builder.bytesEventListener(bytesEventListener)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> defaultValue(V defaultValue) {
            configToApply.add((builder -> builder.defaultValue(defaultValue)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> defaultValueProvider(DefaultValueProvider<K, V> defaultValueProvider) {
            configToApply.add((builder -> builder.defaultValueProvider(defaultValueProvider)));
            return this;
        }

        public ChronicleMapMacheBuilder<K, V> prepareDefaultValueBytes(PrepareValueBytes<K, V> prepareValueBytes) {
            configToApply.add((builder -> builder.prepareDefaultValueBytes(prepareValueBytes)));
            return this;
        }
    }
}