package com.excelian.mache.core;


/**
 * Loads a cache with the values in some datastore.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 * @param <D> the type of the driver for the underlying store
 */
//todo: Would this class be better named MacheDataStore ???
public interface MacheLoader<K, V, D> {

    /**
     * Creates the underlying store.
     */
    void create();

    /**
     * Puts the given entry into the underlying store.
     * @param key the key to put
     * @param value the value to put
     */
    void put(K key, V value);

    /**
     * Removes the given entry from the store.
     * @param key The key to remove.
     */
    void remove(K key);

    /**
     * Loads the value from the store with the given key.
     *
     * @param key the key to find.
     * @return the value associated with the key or null if no value found.
     * @throws Exception if any issues with retrieval
     */
    V load(K key) throws Exception;

    /*
     * Close the connection to the datastore
     */
    void close();


    /*
     * @return A simple name to describe implementers of this class
     */
    String getName();

    /*
     * @return a session to the underlying driver
     */
    //todo: this method is only called in test code - can we remove it and the
    // generic declaration at the top of this class too?
    D getDriverSession();
}
