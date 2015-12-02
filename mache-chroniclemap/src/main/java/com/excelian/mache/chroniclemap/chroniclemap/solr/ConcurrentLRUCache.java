package com.excelian.mache.chroniclemap.chroniclemap.solr;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.openhft.chronicle.map.ChronicleMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A LRU cache implementation based upon ChronicleMap and other techniques to reduce
 * contention and synchronization overhead to utilize multiple CPU cores more effectively.
 * <p>
 * Note that the implementation does not follow a true LRU (least-recently-used) eviction
 * strategy. Instead it strives to remove least recently used items but when the initial
 * cleanup does not remove enough items to reach the 'acceptableWaterMark' limit, it can
 * remove more items forcefully regardless of access order.
 *
 * @since solr 1.4
 */
public class ConcurrentLRUCache<K, V> implements Cache<K, V> {
    private static Logger log = LoggerFactory.getLogger(ConcurrentLRUCache.class);

    private final ConcurrentMap<K, CacheEntry<K>> map = new ConcurrentHashMap<>();
    private final ConcurrentMap<K, V> backingMap;
    private final int upperWaterMark, lowerWaterMark;
    private final ReentrantLock markAndSweepLock = new ReentrantLock(true);
    private final boolean newThreadForCleanup;
    private final Stats stats = new Stats();
    private final int acceptableWaterMark;
    private final EvictionListener<K, V> evictionListener;
    private boolean isCleaning = false;  // not volatile... piggybacked on other volatile vars
    private volatile boolean islive = true;
    private long oldestEntry = 0;  // not volatile, only accessed in the cleaning method
    private CleanupThread cleanupThread;
    private boolean isDestroyed = false;

    public ConcurrentLRUCache(int upperWaterMark, final int lowerWaterMark, int acceptableWatermark,
                              boolean runCleanupThread, boolean runNewThreadForCleanup,
                              EvictionListener<K, V> evictionListener, ConcurrentMap<K, V> backingMap) {
        if (upperWaterMark < 1) throw new IllegalArgumentException("upperWaterMark must be > 0");
        if (lowerWaterMark >= upperWaterMark)
            throw new IllegalArgumentException("lowerWaterMark must be  < upperWaterMark");

        this.backingMap = backingMap;
        newThreadForCleanup = runNewThreadForCleanup;
        this.upperWaterMark = upperWaterMark;
        this.lowerWaterMark = lowerWaterMark;
        this.acceptableWaterMark = acceptableWatermark;
        this.evictionListener = evictionListener;
        if (runCleanupThread) {
            cleanupThread = new CleanupThread(this);
            cleanupThread.start();
        }
    }

    public void setAlive(boolean live) {
        islive = live;
    }

    @Override
    public V get(K key) {
        CacheEntry<K> e = map.get(key);
        if (e == null) {
            if (islive) stats.missCounter.incrementAndGet();
            return null;
        }
        if (islive) e.lastAccessed = stats.accessCounter.incrementAndGet();

        return backingMap.get(key);
    }

    @Override
    public V remove(K key) {
        CacheEntry<K> cacheEntry = map.remove(key);
        if (cacheEntry != null) {
            stats.size.decrementAndGet();
            return backingMap.remove(key);
        }
        return null;
    }

    @Override
    public V put(K key, V val) {
        if (val == null) return null;
        CacheEntry<K> e = new CacheEntry<>(key, stats.accessCounter.incrementAndGet());
        CacheEntry<K> oldCacheEntry = map.put(key, e);
        int currentSize;
        if (oldCacheEntry == null) {
            currentSize = stats.size.incrementAndGet();
        } else {
            currentSize = stats.size.get();
        }
        if (islive) {
            stats.putCounter.incrementAndGet();
        } else {
            stats.nonLivePutCounter.incrementAndGet();
        }
        V oldCacheValue = backingMap.put(key, val);

        // Check if we need to clear out old entries from the cache.
        // isCleaning variable is checked instead of markAndSweepLock.isLocked()
        // for performance because every put invocation will check until
        // the size is back to an acceptable level.
        //
        // There is a race between the check and the call to markAndSweep, but
        // it's unimportant because markAndSweep actually acquires the lock or returns if it can't.
        //
        // Thread safety note: isCleaning read is piggybacked (comes after) other volatile reads
        // in this method.
        if (currentSize > upperWaterMark && !isCleaning) {
            if (newThreadForCleanup) {
                new Thread() {
                    @Override
                    public void run() {
                        markAndSweep();
                    }
                }.start();
            } else if (cleanupThread != null) {
                cleanupThread.wakeThread();
            } else {
                markAndSweep();
            }
        }
        return oldCacheValue == null ? null : oldCacheValue;
    }

    /**
     * Removes items from the cache to bring the size down
     * to an acceptable value ('acceptableWaterMark').
     * <p>
     * It is done in two stages. In the first stage, least recently used items are evicted.
     * If, after the first stage, the cache size is still greater than 'acceptableSize'
     * config parameter, the second stage takes over.
     * <p>
     * The second stage is more intensive and tries to bring down the cache size
     * to the 'lowerWaterMark' config parameter.
     */
    private void markAndSweep() {
        // if we want to keep at least 1000 entries, then timestamps of
        // current through current-1000 are guaranteed not to be the oldest (but that does
        // not mean there are 1000 entries in that group... it's actually anywhere between
        // 1 and 1000).
        // Also, if we want to remove 500 entries, then
        // oldestEntry through oldestEntry+500 are guaranteed to be
        // removed (however many there are there).

        if (!markAndSweepLock.tryLock()) return;
        try {
            long oldestEntry = this.oldestEntry;
            isCleaning = true;
            this.oldestEntry = oldestEntry;     // volatile write to make isCleaning visible

            long timeCurrent = stats.accessCounter.get();
            int sz = stats.size.get();

            int numRemoved = 0;
            int numKept = 0;
            long newestEntry = timeCurrent;
            long newNewestEntry = -1;
            long newOldestEntry = Long.MAX_VALUE;

            int wantToKeep = lowerWaterMark;
            int wantToRemove = sz - lowerWaterMark;

            @SuppressWarnings("unchecked") // generic array's are annoying
                    CacheEntry<K>[] eset = new CacheEntry[sz];
            int eSize = 0;

            // System.out.println("newestEntry="+newestEntry + " oldestEntry="+oldestEntry);
            // System.out.println("items removed:" + numRemoved + " numKept=" + numKept + " esetSz="+ eSize + " sz-numRemoved=" + (sz-numRemoved));

            for (CacheEntry<K> ce : map.values()) {
                // set lastAccessedCopy to avoid more volatile reads
                ce.lastAccessedCopy = ce.lastAccessed;
                long thisEntry = ce.lastAccessedCopy;

                // since the wantToKeep group is likely to be bigger than wantToRemove, check it first
                if (thisEntry > newestEntry - wantToKeep) {
                    // this entry is guaranteed not to be in the bottom
                    // group, so do nothing.
                    numKept++;
                    newOldestEntry = Math.min(thisEntry, newOldestEntry);
                } else if (thisEntry < oldestEntry + wantToRemove) { // entry in bottom group?
                    // this entry is guaranteed to be in the bottom group
                    // so immediately remove it from the map.
                    evictEntry(ce.key);
                    numRemoved++;
                } else {
                    // This entry *could* be in the bottom group.
                    // Collect these entries to avoid another full pass... this is wasted
                    // effort if enough entries are normally removed in this first pass.
                    // An alternate impl could make a full second pass.
                    if (eSize < eset.length - 1) {
                        eset[eSize++] = ce;
                        newNewestEntry = Math.max(thisEntry, newNewestEntry);
                        newOldestEntry = Math.min(thisEntry, newOldestEntry);
                    }
                }
            }

            // System.out.println("items removed:" + numRemoved + " numKept=" + numKept + " esetSz="+ eSize + " sz-numRemoved=" + (sz-numRemoved));
            // TODO: allow this to be customized in the constructor?
            int numPasses = 1; // maximum number of linear passes over the data

            // if we didn't remove enough entries, then make more passes
            // over the values we collected, with updated min and max values.
            while (sz - numRemoved > acceptableWaterMark && --numPasses >= 0) {

                oldestEntry = newOldestEntry == Long.MAX_VALUE ? oldestEntry : newOldestEntry;
                newOldestEntry = Long.MAX_VALUE;
                newestEntry = newNewestEntry;
                newNewestEntry = -1;
                wantToKeep = lowerWaterMark - numKept;
                wantToRemove = sz - lowerWaterMark - numRemoved;

                // iterate backward to make it easy to remove items.
                for (int i = eSize - 1; i >= 0; i--) {
                    CacheEntry<K> ce = eset[i];
                    long thisEntry = ce.lastAccessedCopy;

                    if (thisEntry > newestEntry - wantToKeep) {
                        // this entry is guaranteed not to be in the bottom
                        // group, so do nothing but remove it from the eset.
                        numKept++;
                        // remove the entry by moving the last element to its position
                        eset[i] = eset[eSize - 1];
                        eSize--;

                        newOldestEntry = Math.min(thisEntry, newOldestEntry);

                    } else if (thisEntry < oldestEntry + wantToRemove) { // entry in bottom group?

                        // this entry is guaranteed to be in the bottom group
                        // so immediately remove it from the map.
                        evictEntry(ce.key);
                        numRemoved++;

                        // remove the entry by moving the last element to its position
                        eset[i] = eset[eSize - 1];
                        eSize--;
                    } else {
                        // This entry *could* be in the bottom group, so keep it in the eset,
                        // and update the stats.
                        newNewestEntry = Math.max(thisEntry, newNewestEntry);
                        newOldestEntry = Math.min(thisEntry, newOldestEntry);
                    }
                }
                // System.out.println("items removed:" + numRemoved + " numKept=" + numKept + " esetSz="+ eSize + " sz-numRemoved=" + (sz-numRemoved));
            }


            // if we still didn't remove enough entries, then make another pass while
            // inserting into a priority queue
            if (sz - numRemoved > acceptableWaterMark) {

                oldestEntry = newOldestEntry == Long.MAX_VALUE ? oldestEntry : newOldestEntry;
                newOldestEntry = Long.MAX_VALUE;
                newestEntry = newNewestEntry;
                newNewestEntry = -1;
                wantToKeep = lowerWaterMark - numKept;
                wantToRemove = sz - lowerWaterMark - numRemoved;

                PQueue<K, V> queue = new PQueue<>(wantToRemove);

                for (int i = eSize - 1; i >= 0; i--) {
                    CacheEntry<K> ce = eset[i];
                    long thisEntry = ce.lastAccessedCopy;

                    if (thisEntry > newestEntry - wantToKeep) {
                        // this entry is guaranteed not to be in the bottom
                        // group, so do nothing but remove it from the eset.
                        numKept++;
                        // removal not necessary on last pass.
                        // eset[i] = eset[eSize-1];
                        // eSize--;

                        newOldestEntry = Math.min(thisEntry, newOldestEntry);

                    } else if (thisEntry < oldestEntry + wantToRemove) {  // entry in bottom group?
                        // this entry is guaranteed to be in the bottom group
                        // so immediately remove it.
                        evictEntry(ce.key);
                        numRemoved++;

                        // removal not necessary on last pass.
                        // eset[i] = eset[eSize-1];
                        // eSize--;
                    } else {
                        // This entry *could* be in the bottom group.
                        // add it to the priority queue

                        // everything in the priority queue will be removed, so keep track of
                        // the lowest value that ever comes back out of the queue.

                        // first reduce the size of the priority queue to account for
                        // the number of items we have already removed while executing
                        // this loop so far.
                        queue.myMaxSize = sz - lowerWaterMark - numRemoved;
                        while (queue.size() > queue.myMaxSize && queue.size() > 0) {
                            CacheEntry otherEntry = queue.pop();
                            newOldestEntry = Math.min(otherEntry.lastAccessedCopy, newOldestEntry);
                        }
                        if (queue.myMaxSize <= 0) break;

                        Object o = queue.myInsertWithOverflow(ce);
                        if (o != null) {
                            newOldestEntry = Math.min(((CacheEntry) o).lastAccessedCopy, newOldestEntry);
                        }
                    }
                }

                // Now delete everything in the priority queue.
                // avoid using pop() since order doesn't matter anymore
                for (CacheEntry<K> ce : queue.getValues()) {
                    if (ce == null) continue;
                    evictEntry(ce.key);
                    numRemoved++;
                }

                // System.out.println("items removed:" + numRemoved + " numKept=" + numKept + " initialQueueSize="+ wantToRemove + " finalQueueSize=" + queue.size() + " sz-numRemoved=" + (sz-numRemoved));
            }

            oldestEntry = newOldestEntry == Long.MAX_VALUE ? oldestEntry : newOldestEntry;
            this.oldestEntry = oldestEntry;
        } finally {
            isCleaning = false;  // set before markAndSweep.unlock() for visibility
            markAndSweepLock.unlock();
        }
    }

    private void evictEntry(K key) {
        CacheEntry<K> o = map.remove(key);
        V value = backingMap.remove(key);
        if (o == null) return;
        stats.size.decrementAndGet();
        stats.evictionCounter.incrementAndGet();
        if (evictionListener != null) evictionListener.evictedEntry(o.key, value);
    }
    public int size() {
        return stats.size.get();
    }

    @Override
    public void clear() {
        map.clear();
    }

    public void destroy() {
        try {
            if (cleanupThread != null) {
                cleanupThread.stopThread();
            }
        } finally {
            isDestroyed = true;
        }
    }

    public Stats getStats() {
        return stats;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!isDestroyed && (cleanupThread != null)) {
                log.error("ConcurrentLRUCache created with a thread and was not destroyed prior to finalize(), indicates a bug -- POSSIBLE RESOURCE LEAK!!!");
                destroy();
            }
        } finally {
            super.finalize();
        }
    }

    public void close() {
        ChronicleMap<K, V> backingChronicleMap = (ChronicleMap<K, V>) backingMap;
        backingChronicleMap.close();
    }

    public static interface EvictionListener<K, V> {
        public void evictedEntry(K key, V value);
    }

    private static class PQueue<K, V> extends PriorityQueue<CacheEntry<K>> {
        final Object[] heap;
        int myMaxSize;

        PQueue(int maxSz) {
            super(maxSz);
            heap = getHeapArray();
            myMaxSize = maxSz;
        }

        @SuppressWarnings("unchecked")
        Iterable<CacheEntry<K>> getValues() {
            return (Iterable) Collections.unmodifiableCollection(Arrays.asList(heap));
        }

        @Override
        protected boolean lessThan(CacheEntry a, CacheEntry b) {
            // reverse the parameter order so that the queue keeps the oldest items
            return b.lastAccessedCopy < a.lastAccessedCopy;
        }

        // necessary because maxSize is private in base class
        @SuppressWarnings("unchecked")
        public CacheEntry<K> myInsertWithOverflow(CacheEntry<K> element) {
            if (size() < myMaxSize) {
                add(element);
                return null;
            } else if (size() > 0 && !lessThan(element, (CacheEntry<K>) heap[1])) {
                CacheEntry<K> ret = (CacheEntry<K>) heap[1];
                heap[1] = element;
                updateTop();
                return ret;
            } else {
                return element;
            }
        }
    }

    private static class CacheEntry<K> implements Comparable<CacheEntry<K>> {
        K key;
        volatile long lastAccessed = 0;
        long lastAccessedCopy = 0;


        public CacheEntry(K key, long lastAccessed) {
            this.key = key;
            this.lastAccessed = lastAccessed;
        }

        public void setLastAccessed(long lastAccessed) {
            this.lastAccessed = lastAccessed;
        }

        @Override
        public int compareTo(CacheEntry<K> that) {
            if (this.lastAccessedCopy == that.lastAccessedCopy) return 0;
            return this.lastAccessedCopy < that.lastAccessedCopy ? 1 : -1;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return key.equals(obj);
        }

        @Override
        public String toString() {
            return "key: " + key + " lastAccessed:" + lastAccessed;
        }
    }

    public static class Stats {
        private final AtomicLong accessCounter = new AtomicLong(0),
                putCounter = new AtomicLong(0),
                nonLivePutCounter = new AtomicLong(0),
                missCounter = new AtomicLong();
        private final AtomicInteger size = new AtomicInteger();
        private AtomicLong evictionCounter = new AtomicLong();

        public long getCumulativeLookups() {
            return (accessCounter.get() - putCounter.get() - nonLivePutCounter.get()) + missCounter.get();
        }

        public long getCumulativeHits() {
            return accessCounter.get() - putCounter.get() - nonLivePutCounter.get();
        }

        public long getCumulativePuts() {
            return putCounter.get();
        }

        public long getCumulativeEvictions() {
            return evictionCounter.get();
        }

        public int getCurrentSize() {
            return size.get();
        }

        public long getCumulativeNonLivePuts() {
            return nonLivePutCounter.get();
        }

        public long getCumulativeMisses() {
            return missCounter.get();
        }

        public void add(Stats other) {
            accessCounter.addAndGet(other.accessCounter.get());
            putCounter.addAndGet(other.putCounter.get());
            nonLivePutCounter.addAndGet(other.nonLivePutCounter.get());
            missCounter.addAndGet(other.missCounter.get());
            evictionCounter.addAndGet(other.evictionCounter.get());
            size.set(Math.max(size.get(), other.size.get()));
        }
    }

    private static class CleanupThread extends Thread {
        private WeakReference<ConcurrentLRUCache> cache;

        private boolean stop = false;

        public CleanupThread(ConcurrentLRUCache c) {
            cache = new WeakReference<>(c);
        }

        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    if (stop) break;
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (stop) break;
                ConcurrentLRUCache c = cache.get();
                if (c == null) break;
                c.markAndSweep();
            }
        }

        void wakeThread() {
            synchronized (this) {
                this.notify();
            }
        }

        void stopThread() {
            synchronized (this) {
                stop = true;
                this.notify();
            }
        }
    }
}