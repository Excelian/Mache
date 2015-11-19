package com.excelian.mache.aggregate;

import com.excelian.mache.core.AbstractCacheLoader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

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
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader<String, String, ?> anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_createIsCalledOnTheAggregate();
        then_createIsCalledOn(theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesPutToAWrappedCacheLoader() {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader);
        when_putIsCalledOnTheAggregateWith("key", "value");
        then_putIsCalledOnWith("key", "value", theMockCacheLoader);
    }

    @Test
    public void testItDelegatesPutToAllWrappedCacheLoader() {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader<String, String, ?> anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_putIsCalledOnTheAggregateWith("key", "value");
        then_putIsCalledOnWith("key", "value", theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesRemoveToAWrappedCacheLoader() {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader);
        when_removeIsCalledOnTheAggregateWith("key");
        then_removeIsCalledOnWith("key", theMockCacheLoader);
    }

    @Test
    public void testItDelegatesRemoveToAllWrappedCacheLoader() {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader<String, String, ?> anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_removeIsCalledOnTheAggregateWith("key");
        then_removeIsCalledOnWith("key", theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItReturnsTheFirstNonNullValueForFromTheDelegatesWhenLoadIscalled() throws Exception {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        //mocks and mockito generics don't find the generic-typed method, therefore no generic type dec here
        final AbstractCacheLoader anotherMockCacheLoader = given_a_mockCacheLoader();
        final String expected = "EXPECTED VALUE!";
        when(anotherMockCacheLoader.load("key")).thenReturn(expected);
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_loadIsCalledOnTheAggregateWith("key");
        then_loadReturns(expected);
    }

    @Test
    public void testItDelegatesCloseToAWrappedCacheLoader() throws Exception {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader<String, String, ?> anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_closeIsCalledOnTheAggregate();
        then_closeIsCalledOn(theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesCloseToAllWrappedCacheLoader() throws Exception {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
        final AbstractCacheLoader<String, String, ?> anotherMockCacheLoader = given_a_mockCacheLoader();
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_closeIsCalledOnTheAggregate();
        then_closeIsCalledOn(theMockCacheLoader, anotherMockCacheLoader);
    }

    @Test
    public void testItDelegatesGetNameToAllWrappedCacheLoaders() {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoaderNamed("Mongo");
        final AbstractCacheLoader<String, String, ?> anotherMockCacheLoader = given_a_mockCacheLoaderNamed("Cassandra");
        given_anAggregateCacheLoaderWrapping(theMockCacheLoader, anotherMockCacheLoader);
        when_getNameIsCalledOnTheAggregate();
        then_theNameIs("AggregateCacheLoader[Mongo,Cassandra]");
    }

    @Test
    public void testItDoesNotDelegateGetDriverSessionToAWrappedCacheLoader() {
        final AbstractCacheLoader<String, String, ?> theMockCacheLoader = given_a_mockCacheLoader();
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

    private void when_putIsCalledOnTheAggregateWith(String key, String value) {
        this.aggregateCacheLoader.put(key, value);
    }

    private void when_removeIsCalledOnTheAggregateWith(String key) {
        this.aggregateCacheLoader.remove(key);
    }

    private void when_createIsCalledOnTheAggregate() {
        this.aggregateCacheLoader.create();
    }

    private void then_createIsCalledOn(AbstractCacheLoader<String, String, ?> cacheLoader) {
        verify(cacheLoader).create();
    }

    private void then_createIsCalledOn(AbstractCacheLoader<String, String, ?> cacheLoaderOne,
                                       AbstractCacheLoader<String, String, ?> cacheLoaderTwo
    ) {
        then_createIsCalledOn(cacheLoaderOne);
        then_createIsCalledOn(cacheLoaderTwo);
    }

    private void then_closeIsCalledOn(AbstractCacheLoader<String, String, ?> cacheLoader) {
        verify(cacheLoader).close();
    }

    private void then_closeIsCalledOn(AbstractCacheLoader<String, String, ?> cacheLoaderOne,
                                      AbstractCacheLoader<String, String, ?> cacheLoaderTwo
    ) {
        then_closeIsCalledOn(cacheLoaderOne);
        then_closeIsCalledOn(cacheLoaderTwo);
    }

    private void then_removeIsCalledOnWith(String key, AbstractCacheLoader<String, String, ?> cacheLoader) {
        verify(cacheLoader).remove(key);
    }

    private void then_removeIsCalledOnWith(String key,
                                           AbstractCacheLoader<String, String, ?> cacheLoaderOne,
                                           AbstractCacheLoader<String, String, ?> cacheLoaderTwo
    ) {
        then_removeIsCalledOnWith(key, cacheLoaderOne);
        then_removeIsCalledOnWith(key, cacheLoaderTwo);
    }

    private void then_putIsCalledOnWith(String key, String value, AbstractCacheLoader<String, String, ?> cacheLoader) {
        verify(cacheLoader).put(key, value);
    }

    private void then_putIsCalledOnWith(String key, String value,
                                        AbstractCacheLoader<String, String, ?> cacheLoaderOne,
                                        AbstractCacheLoader<String, String, ?> cacheLoaderTwo
    ) {
        then_putIsCalledOnWith(key, value, cacheLoaderOne);
        then_putIsCalledOnWith(key, value, cacheLoaderTwo);
    }

    private void given_anAggregateCacheLoaderWrapping(AbstractCacheLoader<String, String, ?> cacheLoader) {
        final List<AbstractCacheLoader<String, String, ?>> cacheLoaders = toList(cacheLoader);
        this.aggregateCacheLoader = new AggregateCacheLoader<>(cacheLoaders);
    }

    private List<AbstractCacheLoader<String, String, ?>> toList(AbstractCacheLoader<String, String, ?> cacheLoader) {
        final List<AbstractCacheLoader<String, String, ?>> cacheLoaders = new ArrayList<>();
        cacheLoaders.add(cacheLoader);
        return cacheLoaders;
    }

    private void given_anAggregateCacheLoaderWrapping(AbstractCacheLoader<String, String, ?> cacheLoader,
                                                      AbstractCacheLoader<String, String, ?> cacheLoader2) {
        final List<AbstractCacheLoader<String, String, ?>> cacheLoaders = toList(cacheLoader);
        cacheLoaders.add(cacheLoader2);
        this.aggregateCacheLoader = new AggregateCacheLoader<>(cacheLoaders);
    }

    @SuppressWarnings("Unchecked")
    private AbstractCacheLoader<String, String, ?> given_a_mockCacheLoader() {
        return (AbstractCacheLoader<String, String, ?>) mock(AbstractCacheLoader.class);
    }

    private AbstractCacheLoader<String, String, ?> given_a_mockCacheLoaderNamed(String name) {
        final AbstractCacheLoader<String, String, ?> mock = given_a_mockCacheLoader();
        when(mock.getName()).thenReturn(name);
        return mock;
    }
}
