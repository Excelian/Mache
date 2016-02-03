package com.excelian.mache.rest;

import io.vertx.core.Vertx;

import java.util.function.Function;

/**
 * Entry point to start the Rest service.
 *
 * @implNote This exists purely to hide the Vertx implementation, whilst an interface may separate
 *          the implementation the public API is better off hiding any reference to vertx/vertical.
 */
public class MacheRestService {
    private final Vertx vertx;

    public MacheRestService() {
        vertx = Vertx.vertx();
    }

    /**
     * An async call to start the rest service.
     *
     * @param serviceConfiguration Configuration describing the endpoint
     * @param macheFactory         A delegate that will create new Mache instances given a RequestContext
     */
    public void runAsync(MacheRestServiceConfiguration serviceConfiguration,
                         Function<MacheRestRequestContext, RestManagedMache> macheFactory)
        throws Exception {
        MacheInstanceCache instanceCache = new MacheInstanceCache(macheFactory,
            (time, delegate) -> vertx.setTimer(time, (x) -> delegate.run()));
        MacheVertical vertical = new MacheVertical(serviceConfiguration, instanceCache);
        vertx.deployVerticle(vertical);
    }

    /**
     * An async call to start the rest service using default options.
     *
     * @param macheFactory A delegate that will create new Mache instances given a RequestContext
     */
    public void runAsync(Function<MacheRestRequestContext, RestManagedMache> macheFactory) throws Exception {
        runAsync(new MacheRestServiceConfiguration(), macheFactory);
    }

    /**
     * Stops the REST service.
     */
    public void stopAsync() {
        vertx.close();
    }
}
