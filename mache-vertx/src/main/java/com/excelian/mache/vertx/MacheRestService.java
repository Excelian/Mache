package com.excelian.mache.vertx;

import com.excelian.mache.core.Mache;
import io.vertx.core.Vertx;

import java.util.function.Supplier;

/**
 * Entry point to start the Rest service
 * @implNote This exists purely to hide the Vertx implementation, whilst an interface may separate
 * the implementation the public API is better off hiding any reference to vertx/vertical
 */
public class MacheRestService {

    /**
     * A blocking call to start the rest service
     */
    public void run(Supplier<Mache<String, String>> macheFactory) throws Exception {
        MacheVertical vertical = new MacheVertical(new MacheInstanceCache(macheFactory));
        Vertx.vertx().deployVerticle(vertical);
    }
}
