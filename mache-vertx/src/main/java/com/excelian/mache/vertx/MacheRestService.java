package com.excelian.mache.vertx;

import com.excelian.mache.factory.MacheFactory;

/**
 * Entry point to start the Rest service
 * @implNote This exists purely to hide the Vertx implementation, whilst an interface may separate
 * the implementation the public API is better off hiding any reference to vertx/vertical
 */
public class MacheRestService {

    /**
     * A blocking call to start the rest service
     */
    public void run(MacheFactory macheFactory) throws Exception {
        MacheVertical vertical = new MacheVertical(new MacheInstanceCache(macheFactory));
        vertical.start();
    }
}
