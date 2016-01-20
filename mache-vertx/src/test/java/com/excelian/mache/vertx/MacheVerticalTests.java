package com.excelian.mache.vertx;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.google.common.net.MediaType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.util.function.Supplier;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;

@RunWith(VertxUnitRunner.class)
public class MacheVerticalTests {
    private Vertx vertx;
    private MacheInstanceCache instanceCache;
    private MacheVertical vertical;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        Supplier<Mache<String, String>> factory = () -> {
            try {
                return mache(String.class, String.class)
                        .cachedBy(guava())
                        .storedIn(new StorageProvisioner() {
                            @Override
                            public <K, V> MacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
                                return new HashMapCacheLoader<>(valueType);
                            }
                        })
                        .withNoMessaging()
                        .macheUp();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        instanceCache = new MacheInstanceCache(factory);
        vertical = new MacheVertical(new MacheRestServiceConfiguration(), instanceCache);
        vertx.deployVerticle(vertical, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void getRootShouldReturnDocumentation(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/",
                response -> response.bodyHandler(body -> {
                    // Check for swagger to be present as the Mache references are loaded via JS scripts
                    context.assertTrue(body.toString().toLowerCase().contains("swagger"));
                    async.complete();
                }));
    }

    @Test
    public void externalAddressShouldReceive401ReplyWhenLocalOnly(TestContext context) {
        final Async async = context.async();


        MacheRestServiceConfiguration configuration = new MacheRestServiceConfiguration(8080, "localhost", true);

        // Need a vertical with the new config
        vertx.undeploy(vertx.deploymentIDs().iterator().next());
        vertical = new MacheVertical(configuration, instanceCache);
        vertx.deployVerticle(vertical, context.asyncAssertSuccess());

        // Show the address as external
        vertical.setLocalAddressCheck(address -> false);

        vertx.createHttpClient().getNow(8080, "localhost", "/map/names/2",
                response -> {
                    context.assertEquals(401, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void getShouldReturnKnownKey(TestContext context) {
        final Async async = context.async();

        instanceCache.putKey("names", "1", "{'name': 'ben'");
        vertx.createHttpClient().getNow(8080, "localhost", "/map/names/1",
                response -> response.handler(body -> {
                    context.assertTrue(body.toString().contains("ben"));
                    async.complete();
                }));
    }

    @Test
    public void getShouldReturn400MissingKey(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/map/names/2",
                response -> {
                    context.assertEquals(400, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void deleteShouldRemoveKeyFromMap(TestContext context) {
        final Async async = context.async();

        instanceCache.putKey("names", "1", "{'name': 'ben'}");
        vertx.createHttpClient().delete(8080, "localhost", "/map/names/1",
                response -> {
                    context.assertEquals(200, response.statusCode());
                    String value = instanceCache.getKey("names", "1");
                    context.assertEquals(null, value);
                    async.complete();
                })
                .end();
    }

    @Test
    public void putShouldAddKeyToMap(TestContext context) {
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
