package com.excelian.mache.overflow;


import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.MacheLoader;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import java.util.Arrays;

/**
 * Created by jbowkett on 18/11/2015.
 */
public class TestAggregateCacheLoader {

    private AggregateCacheLoader<String, String> aggregateCacheLoader;
    private String name;
    private String actualValueLoaded;

    @Test
    public void testItDelegatesCreateToAWrappedCacheLoader() {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader);
        when_createIsCalledOnTheAggregate();
        then_createIsCalledOn(theMockCacheLoader);
    }

    @Test
    public void testItDelegatesCreateToAllWrappedCacheLoaders() {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_createIsCalledOnTheAggregate();
        then_createIsCalledOn(theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesPutToAWrappedCacheLoader() {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader);
        when_putIsCalledOnTheAggregateWith("key", "value");
        then_putIsCalledOnWith("key", "value", theMockCacheLoader);
    }

    @Test
    public void testItDelegatesPutToAllWrappedCacheLoader() {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_putIsCalledOnTheAggregateWith("key", "value");
        then_putIsCalledOnWith("key", "value", theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesRemoveToAWrappedCacheLoader() {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader);
        when_removeIsCalledOnTheAggregateWith("key");
        then_removeIsCalledOnWith("key", theMockCacheLoader);
    }

    @Test
    public void testItDelegatesRemoveToAllWrappedCacheLoader() {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_removeIsCalledOnTheAggregateWith("key");
        then_removeIsCalledOnWith("key", theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItReturnsTheFirstNonNullValueForFromTheDelegatesWhenLoadIscalled() throws Exception {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoader();
        final String expected = "EXPECTED VALUE!";
        when(anotherMockCacheLoader.load("key")).thenReturn(expected);
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_loadIsCalledOnTheAggregateWith("key");
        then_loadReturns(expected);
    }

    @Test
    public void testItDelegatesCloseToAWrappedCacheLoader() throws Exception {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_closeIsCalledOnTheAggregate();
        then_closeIsCalledOn(theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesCloseToAllWrappedCacheLoader() throws Exception {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_closeIsCalledOnTheAggregate();
        then_closeIsCalledOn(theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesGetNameToAllWrappedCacheLoaders() {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoaderNamed("Mongo");
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoaderNamed("Cassandra");
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_getNameIsCalledOnTheAggregate();
        then_theNameIs("AggregateCacheLoader[Mongo,Cassandra]");
    }

    @Test
    public void testItDoesNotDelegateGetDriverSessionToAWrappedCacheLoader() {
        final AbstractCacheLoader theMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader);
        when_getDriverSessionIsCalledOnTheAggregate();
        then_getDriverSessionIsNotCalledOn(theMockCacheLoader);
    }

    private void then_loadReturns(String expected) {
        assertEquals(expected, this.actualValueLoaded);
    }

    private void then_theNameIs(String expectedName) {
        assertEquals(expectedName, name);
    }

    private void when_getNameIsCalledOnTheAggregate() {
        name = this.aggregateCacheLoader.getName();
    }

    private void then_getDriverSessionIsNotCalledOn(AbstractCacheLoader theMockCacheLoader) {
        verify(theMockCacheLoader, never()).getDriverSession();
    }

    private void when_getDriverSessionIsCalledOnTheAggregate() {
        this.aggregateCacheLoader.getDriverSession();
    }


    private void when_loadIsCalledOnTheAggregateWith(String key) throws Exception {
        actualValueLoaded = this.aggregateCacheLoader.load(key);
    }

    private void when_closeIsCalledOnTheAggregate() throws Exception {
        this.aggregateCacheLoader.close();
    }

    @SuppressWarnings("Unchecked")
    private void then_putIsCalledOnWith(String key, String value, AbstractCacheLoader... cacheLoaders) {
        Arrays.stream(cacheLoaders).forEach(
            cacheLoader -> verify(cacheLoader).put(key, value)
        );
    }

    @SuppressWarnings("Unchecked")
    private void then_loadIsCalledOnWith(String key, AbstractCacheLoader... cacheLoaders) {
        Arrays.stream(cacheLoaders).forEach(
            cacheLoader -> {
                try {
                    verify(cacheLoader).load(key);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Exception : " + e.getMessage());
                }
            }
        );
    }

    @SuppressWarnings("Unchecked")
    private void then_closeIsCalledOn(AbstractCacheLoader... cacheLoaders) {
        Arrays.stream(cacheLoaders).forEach(
            cacheLoader -> verify(cacheLoader).close()
        );
    }

    @SuppressWarnings("Unchecked")
    private void then_removeIsCalledOnWith(String key, AbstractCacheLoader... cacheLoaders) {
        Arrays.stream(cacheLoaders).forEach(
            cacheLoader -> verify(cacheLoader).remove(key)
        );
    }

    private void when_putIsCalledOnTheAggregateWith(String key, String value) {
        this.aggregateCacheLoader.put(key, value);
    }

    private void when_removeIsCalledOnTheAggregateWith(String key) {
        this.aggregateCacheLoader.remove(key);
    }


    private void when_createIsCalledOnTheAggregate() {
        this.aggregateCacheLoader.create();
    }

    private void then_createIsCalledOn(AbstractCacheLoader... cacheLoaders) {
        Arrays.stream(cacheLoaders).forEach(cacheLoader -> {
            verify(cacheLoader).create();
        });

    }

    private void given_anAggregateCacheLoaderWrapping(AbstractCacheLoader... cacheLoaders) {
        this.aggregateCacheLoader = new AggregateCacheLoader<>(cacheLoaders);
    }

    @SuppressWarnings("Unchecked")
    private AbstractCacheLoader<String, String, ?> given_a_mockCacheLoader() {
        return (AbstractCacheLoader<String, String, ?>) mock(AbstractCacheLoader.class);
    }

    @SuppressWarnings("Unchecked")
    private AbstractCacheLoader<String, String, ?> given_a_mockCacheLoaderNamed(String name) {
        final AbstractCacheLoader<String, String, ?> mock = (AbstractCacheLoader<String, String, ?>) mock(AbstractCacheLoader.class);
        when(mock.getName()).thenReturn(name);
        return mock;
    }
}
