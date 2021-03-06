package com.excelian.mache.rest;

import com.excelian.mache.core.HashMapCacheLoader;
import com.google.common.net.MediaType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Function;

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

        Function<MacheRestRequestContext, RestManagedMache> factory = (request) -> {
            try {
                return new RestManagedMache(mache(String.class, String.class)
                    .cachedBy(guava())
                    .storedIn((keyType, valueType) -> new HashMapCacheLoader<>(valueType))
                    .withNoMessaging()
                    .macheUp(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        instanceCache = new MacheInstanceCache(factory, (x, y) -> {
        });
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

    @Ignore("Intermittently failing on the build server with a timeout exception")
    @Test
    public void externalAddressShouldReceive401ReplyWhenLocalOnly(TestContext context) {
        MacheRestServiceConfiguration configuration = new MacheRestServiceConfiguration(8080, "localhost", true);


        final Async asyncStop = context.async();
        // Need a vertical with the new config
        vertx.undeploy(vertx.deploymentIDs().iterator().next(), (x) -> asyncStop.complete());
        asyncStop.await();
        final Async asyncStart = context.async();
        vertical = new MacheVertical(configuration, instanceCache);
        vertx.deployVerticle(vertical, (x) -> asyncStart.complete());

        asyncStart.await();

        final Async asyncRun = context.async();

        // Show the address as external
        vertical.setLocalAddressCheck(address -> false);

        vertx.createHttpClient().getNow(8080, "localhost", "/map/names/2",
            response -> {
                context.assertEquals(401, response.statusCode());
                asyncRun.complete();
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
    public void getShouldReturn404MissingKey(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/map/names/2",
            response -> {
                context.assertEquals(404, response.statusCode());
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
                context.assertNull(value);
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
