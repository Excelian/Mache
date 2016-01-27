package com.excelian.mache.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * A vertx vertical is synonymous to an Actor. This class will register the routes that we are interested in
 * and forward calls to a MacheInstanceCache
 * <p>
 * Threading/synchronisation is handled by the map instances.
 */
public class MacheVertical extends AbstractVerticle {

    /**
     * Utility to detect if a specific IP is a loopback address or bound on any local NIC.
     */
    @FunctionalInterface
    public interface LocalAddressCheck {
        boolean isLocalAddress(InetAddress address);
    }

    private static final Logger LOG = LoggerFactory.getLogger(MacheVertical.class);
    private final MacheRestServiceConfiguration serviceConfiguration;
    private final MacheInstanceCache instanceCache;

    private LocalAddressCheck localAddressCheck = this::runLocalAddressCheck;

    private boolean runLocalAddressCheck(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            return true;
        }

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    public MacheVertical(MacheRestServiceConfiguration serviceConfiguration, MacheInstanceCache instanceCache) {
        this.serviceConfiguration = serviceConfiguration;
        this.instanceCache = instanceCache;
    }

    public void setLocalAddressCheck(LocalAddressCheck localAddressCheck) {
        this.localAddressCheck = localAddressCheck;
    }

    @Override
    public void start() throws Exception {
        // Entry point to the REST service
        Router router = Router.router(vertx);
        registerRoutes(router);

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(serviceConfiguration.getBindPort(), serviceConfiguration.getBindIp(),
                    handler -> {
                        if (handler.succeeded()) {
                            LOG.info("Running on http://{}:{}/",
                                    serviceConfiguration.getBindIp(), serviceConfiguration.getBindPort());
                        } else {
                            LOG.error("Failed to listen on port {}", serviceConfiguration.getBindPort());
                        }
                    });
    }

    private void registerRoutes(Router router) {
        router.route().handler(context -> {
            try {
                boolean isLocal = localAddressCheck.isLocalAddress(
                        InetAddress.getByName(context.request().remoteAddress().host()));

                if (serviceConfiguration.isLocalOnly() && !isLocal) {
                    context.fail(401);
                } else {
                    context.next();
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        router.route("/map/*").handler(BodyHandler.create());

        router.exceptionHandler(this::handleException);

        router.route(HttpMethod.DELETE, "/map/:mapName/:key")
                .handler(this::handleDeleteMap);
        router.route(HttpMethod.GET, "/map/:mapName/:key")
                .handler(this::handleGetMap); // avoid static handler interception
        router.route(HttpMethod.PUT, "/map/:mapName/:key")
                .handler(this::handlePutMap);

        // Register static handler last to avoid GET on a map URL being intercepted
        router.route().handler(StaticHandler.create());
    }

    private void handleException(Throwable throwable) {
        LOG.error("Vertx server exception {}", throwable);
    }

    private void handleGetMap(RoutingContext req) {
        String mapName = req.request().getParam("mapName");
        String key = req.request().getParam("key");

        try {
            String value = instanceCache.getKey(mapName, key);
            if (value == null) {
                req.response()
                        .setStatusCode(400)
                        .end("key not found");
            } else {
                req.response().end(value);
            }
        } catch (Exception e) {
            LOG.error("Failed to get key {} from map {}", key, mapName, e);
            req.fail(500);
        }
    }

    private void handleDeleteMap(RoutingContext req) {
        String mapName = req.request().getParam("mapName");
        String key = req.request().getParam("key");

        try {
            instanceCache.removeKey(mapName, key);
            req.response().end(String.format("removed key %s from %s map", key, mapName));
        } catch (Exception e) {
            LOG.error("Failed to remove key {} from map {}", key, mapName, e);
            req.fail(500);
        }
    }

    private void handlePutMap(RoutingContext req) {
        String mapName = req.request().getParam("mapName");
        String key = req.request().getParam("key");
        String value = req.getBodyAsString();

        try {
            instanceCache.putKey(mapName, key, value);
            req.response().end(String.format("PUT %s %s", mapName, key));
        } catch (Exception e) {
            LOG.error("Failed to put key {} to map {}", key, mapName, e);
            req.fail(500);
        }
    }
}
