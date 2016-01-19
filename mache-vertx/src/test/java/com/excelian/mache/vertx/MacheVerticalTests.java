package com.excelian.mache.vertx;

import com.excelian.mache.builder.NoMessagingProvisioner;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.factory.MacheFactory;
import com.google.common.net.MediaType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;

@RunWith(VertxUnitRunner.class)
public class MacheVerticalTests {
    private Vertx vertx;
    private MacheInstanceCache instanceCache;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        MacheFactory factory = new MacheFactory(guava(),
                        new StorageProvisioner() {
                            @Override
                            public <K, V> MacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
                                return new HashMapCacheLoader<>(valueType);
                            }
                        }, new NoMessagingProvisioner());

        instanceCache = new MacheInstanceCache(factory);
        MacheVertical vertical = new MacheVertical(instanceCache);

        vertx.deployVerticle(vertical);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void Get_Should_Return_Known_Key(TestContext context) {
        final Async async = context.async();

        instanceCache.putKey("names", "1", "{'name': 'ben'");
        vertx.createHttpClient().getNow(8080, "localhost", "/map/names/1",
                response -> response.handler(body -> {
                    context.assertTrue(body.toString().contains("ben"));
                    async.complete();
                }));
    }

    @Test
    public void Get_Should_Return_400_Missing_Key(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/map/names/2",
                response -> {
                    context.assertEquals(400, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void Delete_Should_Remove_Key_From_Map(TestContext context) {
        final Async async = context.async();

        instanceCache.putKey("names", "1", "{'name': 'ben'}");
        vertx.createHttpClient().delete(8080, "localhost", "/map/names/1",
                response -> {
                    context.assertEquals(200, response.statusCode());
                    context.assertTrue(instanceCache.getKey("names", "1") == null);
                    async.complete();
                });
    }

    @Test
    public void Put_Should_Add_Key_To_Map(TestContext context) {
        final Async async = context.async();

        instanceCache.putKey("names", "1", "{'name': 'ben'");
        String jsonData = "{'name': 'ben'}";
        vertx.createHttpClient().put(8080, "localhost", "/map/names/1",
                response -> {
                    context.assertEquals(200, response.statusCode());
                    context.assertEquals(jsonData, instanceCache.getKey("names", "1"));
                    async.complete();
                })
        .putHeader(HttpHeaders.CONTENT_LENGTH, jsonData.length() + "")
        .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        .write(jsonData)
        .end();
    }
}
