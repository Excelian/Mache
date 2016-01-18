package com.excelian.mache.vertx;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.builder.MessagingProvisioner;
import com.excelian.mache.builder.StorageProvisioner;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * A vertx vertical is synonymous to an Actor. This class will register the routes that we are interested in
 * and forward calls to a MacheInstanceCache
 * <p>
 * TODO threading/synchronisation
 */
public class MacheVertical {

    private final MacheInstanceCache instanceCache;

    public MacheVertical(CacheProvisioner cacheProvisioner,
                         StorageProvisioner storageProvisioner,
                         MessagingProvisioner messagingProvisioner) {
        instanceCache = new MacheInstanceCache(storageProvisioner, cacheProvisioner, messagingProvisioner);
    }

    public void run() {
        // Entry point to the REST service
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        router.route().handler(StaticHandler.create());
        router.route("/map/*").handler(BodyHandler.create());

        router.route(HttpMethod.DELETE, "/map/:mapName")
                .handler(this::handleDeleteMap);
        router.route(HttpMethod.GET, "/map/:mapName/:key")
                .order(-1)
                .handler(this::handleGetMap); // avoid static handler interception
        router.route(HttpMethod.PUT, "/map/:mapName/:key")
                .handler(this::handlePutMap);

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(8080, handler -> {
                    if (handler.succeeded()) {
                        System.out.println("http://localhost:8080/");
                    } else {
                        System.err.println("Failed to listen on port 8080");
                    }
                });
    }

    private void handleGetMap(RoutingContext req) {
        String mapName = req.request().getParam("mapName");
        String key = req.request().getParam("key");

        String value = null;
        try {
            value = instanceCache.getKey(mapName, key);
        } catch (Exception e) {
            // key not present
        }

        req.response().end(value == null ? "" : value);
    }

    private void handleDeleteMap(RoutingContext req) {
        String mapName = req.request().getParam("mapName");

        instanceCache.deleteMap(mapName);

        req.response().end("Deleted map " + mapName);
    }

    private void handlePutMap(RoutingContext req) {
        String mapName = req.request().getParam("mapName");
        String key = req.request().getParam("key");
        String value = req.getBodyAsString();

        instanceCache.putKey(mapName, key, value);

        req.response().end("PUT " + mapName + " " + key);
    }
}
