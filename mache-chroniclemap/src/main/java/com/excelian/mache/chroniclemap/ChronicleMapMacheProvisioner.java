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

    private int upperWatermark = 10000;
    private int lowerWatermark = 8500;
    private int acceptableWatermark = 9250;
    private boolean runCleanupThread = true;
    private boolean runNewThreadForCleanup = false;
    private ConcurrentLRUCache.EvictionListener<K, V> evictionListener;
    private File persistedTo;
    private final List<ChronicleMapBuilderConfig<K, V>> configToApply = new ArrayList<>();

    public static <K, V> ChronicleMapMacheProvisioner<K, V> chronicleMap(Class<K> keyType, Class<V> valueType) {
        return new ChronicleMapMacheProvisioner<>();
    }

    @Override
    public Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V> cacheLoader) {
        ChronicleMapBuilder<K, V> chronicleMapBuilder = ChronicleMapBuilder.of(keyType, valueType).entries(11500);
        applyConfig(chronicleMapBuilder);

        ConcurrentMap<K, V> map = buildMap(chronicleMapBuilder);

        ConcurrentLRUCache<K, V> cache = new ConcurrentLRUCache<>(upperWatermark, lowerWatermark,
                acceptableWatermark, runCleanupThread, runNewThreadForCleanup, evictionListener, map);

        return new ChronicleMapMache<>(cacheLoader, cache);
    }

    private void applyConfig(ChronicleMapBuilder<K, V> chronicleMapBuilder) {
        configToApply.stream().forEach((config) -> config.apply(chronicleMapBuilder));
    }

    private ConcurrentMap<K, V> buildMap(ChronicleMapBuilder<K, V> chronicleMapBuilder) {
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
        return map;
    }

    /**
     * Adds a config call that needs to be applied to the builder when building the backing ChronicleMap.
     *
     * @param <K> The key type of the backing Chronicle Map.
     * @param <V> The value type of the backing Chronicle Map.
     */
    @FunctionalInterface
    public interface ChronicleMapBuilderConfig<K, V> {
        ChronicleMapBuilder<K, V> apply(ChronicleMapBuilder<K, V> builder);
    }

    private static final double DEFAULT_BUFFER = 0.15;

    /**
     * Sets the size for the ChronicleMap cache. <bold>Note Well</bold>: Due to the fact ChronicleMap will break if
     * we attempt to add more entries than specified, and that ConcurrentLRUCache can be configured to
     * run its eviction in another thread, we add a small buffer to help ensure entries are removed by the LRU
     * eviction before hitting the ChronicleMap size. Be aware this can still fail if adding elements to the cache
     * quickly while running the CleanUpThread/running clean up in a new thread. See {@link #size(int, double)}
     * for another size method with a configurable buffer.
     *
     * @param size The desired size of the cache.
     * @return The builder being built.
     * @see ChronicleMapBuilder#entries(long)
     */
    public ChronicleMapMacheProvisioner<K, V> size(int size) {
        return size(size, DEFAULT_BUFFER);
    }

    /**
     * As in {@link #size(int)}, sets the size of the cache but provides a configurable buffer parameter.
     *
     * @param size   The desired size of the cache.
     * @param buffer The buffer to add to the ChronicleMap size as a percentage of size - i.e. passing the value of
     *               0.15 would provide a 15% buffer.
     * @return The builder being built.
     * @see ChronicleMapBuilder#entries(long)
     */
    public ChronicleMapMacheProvisioner<K, V> size(int size, double buffer) {
        this.upperWatermark = size;
        this.acceptableWatermark = size - (int) Math.ceil(size * (buffer / 2));
        this.lowerWatermark = size - (int) Math.floor(size * buffer) - 1;

        this.configToApply.add((builder -> builder.entries(size + (int) Math.ceil(size * buffer))));
        return this;
    }

    public ChronicleMapMacheProvisioner<K, V> persistingTo(File persistedTo) {
        this.persistedTo = persistedTo;
        return this;
    }

    /**
     * Run a separate daemon thread to evict least recently used elements from the cache.
     */
    public ChronicleMapMacheProvisioner<K, V> withRunCleanupThread() {
        this.runCleanupThread = true;
        this.runNewThreadForCleanup = false;
        return this;
    }

    /**
     * Spawns a new thread to evict least recently used elements from the cache on each put that occurs when
     * the cache is greater than the specified size.
     */
    public ChronicleMapMacheProvisioner<K, V> withRunNewThreadForCleanup() {
        this.runCleanupThread = false;
        this.runNewThreadForCleanup = true;
        return this;
    }

    /**
     * Adds an EvictionListener to the cache.
     */
    public ChronicleMapMacheProvisioner<K, V> withEvictionListener(
            ConcurrentLRUCache.EvictionListener<K, V> evictionListener) {
        this.evictionListener = evictionListener;
        return this;
    }

    /**
     * Pass through to averageKeySize on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#averageKeySize(double)
     */
    public ChronicleMapMacheProvisioner<K, V> averageKeySize(double averageKeySize) {
        configToApply.add((builder -> builder.averageKeySize(averageKeySize)));
        return this;
    }

    /**
     * Pass through to constantKeySizeBySample on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#constantKeySizeBySample(Object)
     */
    public ChronicleMapMacheProvisioner<K, V> constantKeySizeBySample(K sampleKey) {
        configToApply.add((builder -> builder.constantKeySizeBySample(sampleKey)));
        return this;
    }

    /**
     * Pass through to averageValueSize on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#averageValueSize(double)
     */
    public ChronicleMapMacheProvisioner<K, V> averageValueSize(double averageValueSize) {
        configToApply.add((builder -> builder.averageValueSize(averageValueSize)));
        return this;
    }

    /**
     * Pass through to constantValueSizeBySample on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#constantValueSizeBySample(Object)
     */
    public ChronicleMapMacheProvisioner<K, V> constantValueSizeBySample(V sampleValue) {
        configToApply.add((builder -> builder.constantValueSizeBySample(sampleValue)));
        return this;
    }

    /**
     * Pass through to actualChunkSize on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#actualChunkSize(int)
     */
    public ChronicleMapMacheProvisioner<K, V> actualChunkSize(int actualChunkSize) {
        configToApply.add((builder -> builder.actualChunkSize(actualChunkSize)));
        return this;
    }

    /**
     * Pass through to maxChunksPerEntry on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#maxChunksPerEntry(int)
     */
    public ChronicleMapMacheProvisioner<K, V> maxChunksPerEntry(int maxChunksPerEntry) {
        configToApply.add((builder -> builder.maxChunksPerEntry(maxChunksPerEntry)));
        return this;
    }

    /**
     * Pass through to entriesPerSegment on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#entriesPerSegment(long)
     */
    public ChronicleMapMacheProvisioner<K, V> entriesPerSegment(long entriesPerSegment) {
        configToApply.add((builder -> builder.entriesPerSegment(entriesPerSegment)));
        return this;
    }

    /**
     * Pass through to actualChunksPerSegment on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#actualChunksPerSegment(long)
     */
    public ChronicleMapMacheProvisioner<K, V> actualChunksPerSegment(long actualChunksPerSegment) {
        configToApply.add((builder -> builder.actualChunksPerSegment(actualChunksPerSegment)));
        return this;
    }

    /**
     * Pass through to minSegments on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#minSegments(int)
     */
    public ChronicleMapMacheProvisioner<K, V> minSegments(int minSegments) {
        configToApply.add((builder -> builder.minSegments(minSegments)));
        return this;
    }

    /**
     * Pass through to actualSegments on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#actualSegments(int)
     */
    public ChronicleMapMacheProvisioner<K, V> actualSegments(int actualSegments) {
        configToApply.add((builder -> builder.actualSegments(actualSegments)));
        return this;
    }

    /**
     * Pass through to averageKeySize on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#lockTimeOut(long, TimeUnit)
     */
    public ChronicleMapMacheProvisioner<K, V> lockTimeOut(long lockTimeOut, TimeUnit timeUnit) {
        configToApply.add((builder -> builder.lockTimeOut(lockTimeOut, timeUnit)));
        return this;
    }

    /**
     * Pass through to errorListener on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#errorListener(ChronicleHashErrorListener)
     */
    public ChronicleMapMacheProvisioner<K, V> errorListener(ChronicleHashErrorListener errorListener) {
        configToApply.add((builder -> builder.errorListener(errorListener)));
        return this;
    }

    /**
     * Pass through to putReturnsNull on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#putReturnsNull(boolean)
     */
    public ChronicleMapMacheProvisioner<K, V> putReturnsNull(boolean putReturnsNull) {
        configToApply.add((builder -> builder.putReturnsNull(putReturnsNull)));
        return this;
    }

    /**
     * Pass through to removeReturnsNull on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#removeReturnsNull(boolean)
     */
    public ChronicleMapMacheProvisioner<K, V> removeReturnsNull(boolean removeReturnsNull) {
        configToApply.add((builder -> builder.removeReturnsNull(removeReturnsNull)));
        return this;
    }

    /**
     * Pass through to bytesMarshallerFactory on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#bytesMarshallerFactory(BytesMarshallerFactory)
     */
    public ChronicleMapMacheProvisioner<K, V> bytesMarshallerFactory(BytesMarshallerFactory bytesMarshallerFactory) {
        configToApply.add((builder -> builder.bytesMarshallerFactory(bytesMarshallerFactory)));
        return this;
    }

    /**
     * Pass through to objectSerializer on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#objectSerializer(ObjectSerializer)
     */
    public ChronicleMapMacheProvisioner<K, V> objectSerializer(ObjectSerializer objectSerializer) {
        configToApply.add((builder -> builder.objectSerializer(objectSerializer)));
        return this;
    }

    /**
     * Pass through to keyMarshaller on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#keyMarshaller(BytesMarshaller)
     */
    public ChronicleMapMacheProvisioner<K, V> keyMarshaller(BytesMarshaller<? super K> keyMarshaller) {
        configToApply.add((builder -> builder.keyMarshaller(keyMarshaller)));
        return this;
    }

    /**
     * Pass through to keyMarshallers on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#keyMarshallers(BytesWriter, BytesReader)
     */
    public ChronicleMapMacheProvisioner<K, V> keyMarshallers(BytesWriter<K> keyWriter, BytesReader<K> keyReader) {
        configToApply.add((builder -> builder.keyMarshallers(keyWriter, keyReader)));
        return this;
    }

    /**
     * Pass through to keySizeMarshaller on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#keySizeMarshaller(SizeMarshaller)
     */
    public ChronicleMapMacheProvisioner<K, V> keySizeMarshaller(SizeMarshaller keySizeMarshaller) {
        configToApply.add((builder -> builder.keySizeMarshaller(keySizeMarshaller)));
        return this;
    }

    /**
     * Pass through to keyDeserializationFactory on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#keyDeserializationFactory(ObjectFactory)
     */
    public ChronicleMapMacheProvisioner<K, V> keyDeserializationFactory(ObjectFactory<K> keyDeserializationFactory) {
        configToApply.add((builder -> builder.keyDeserializationFactory(keyDeserializationFactory)));
        return this;
    }

    /**
     * Pass through to immutableKeys on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#immutableKeys()
     */
    public ChronicleMapMacheProvisioner<K, V> immutableKeys() {
        configToApply.add((ChronicleMapBuilder::immutableKeys));
        return this;
    }

    /**
     * Pass through to valueMarshaller on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#valueMarshaller(BytesMarshaller)
     */
    public ChronicleMapMacheProvisioner<K, V> valueMarshaller(BytesMarshaller<? super V> valueMarshaller) {
        configToApply.add((builder -> builder.valueMarshaller(valueMarshaller)));
        return this;
    }

    /**
     * Pass through to valueMarshallers on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#valueMarshallers(BytesWriter, BytesReader)
     */
    public ChronicleMapMacheProvisioner<K, V> valueMarshallers(BytesWriter<V> valueWriter, BytesReader<V> valueReader) {
        configToApply.add((builder -> builder.valueMarshallers(valueWriter, valueReader)));
        return this;
    }

    /**
     * Pass through to valueSizeMarshaller on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#valueSizeMarshaller(SizeMarshaller)
     */
    public ChronicleMapMacheProvisioner<K, V> valueSizeMarshaller(SizeMarshaller valueSizeMarshaller) {
        configToApply.add((builder -> builder.valueSizeMarshaller(valueSizeMarshaller)));
        return this;
    }

    /**
     * Pass through to valueDeserializationFactory on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#valueDeserializationFactory(ObjectFactory)
     */
    public ChronicleMapMacheProvisioner<K, V> valueDeserializationFactory(ObjectFactory<V> valDeserializationFactory) {
        configToApply.add((builder -> builder.valueDeserializationFactory(valDeserializationFactory)));
        return this;
    }

    /**
     * Pass through to eventListener on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#eventListener(MapEventListener)
     */
    public ChronicleMapMacheProvisioner<K, V> eventListener(MapEventListener<K, V> eventListener) {
        configToApply.add((builder -> builder.eventListener(eventListener)));
        return this;
    }

    /**
     * Pass through to bytesEventListener on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#bytesEventListener(BytesMapEventListener)
     */
    public ChronicleMapMacheProvisioner<K, V> bytesEventListener(BytesMapEventListener bytesEventListener) {
        configToApply.add((builder -> builder.bytesEventListener(bytesEventListener)));
        return this;
    }

    /**
     * Pass through to defaultValue on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#defaultValue(Object)
     */
    public ChronicleMapMacheProvisioner<K, V> defaultValue(V defaultValue) {
        configToApply.add((builder -> builder.defaultValue(defaultValue)));
        return this;
    }

    /**
     * Pass through to defaultValueProvider on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#defaultValueProvider(DefaultValueProvider)
     */
    public ChronicleMapMacheProvisioner<K, V> defaultValueProvider(DefaultValueProvider<K, V> defaultValueProvider) {
        configToApply.add((builder -> builder.defaultValueProvider(defaultValueProvider)));
        return this;
    }

    /**
     * Pass through to prepareDefaultValueBytes on ChronicleMapBuilder.
     *
     * @see ChronicleMapBuilder#prepareDefaultValueBytes(PrepareValueBytes)
     */
    public ChronicleMapMacheProvisioner<K, V> prepareDefaultValueBytes(PrepareValueBytes<K, V> prepareValueBytes) {
        configToApply.add((builder -> builder.prepareDefaultValueBytes(prepareValueBytes)));
        return this;
    }
}