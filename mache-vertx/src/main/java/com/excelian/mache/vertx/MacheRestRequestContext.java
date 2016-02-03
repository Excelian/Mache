package com.excelian.mache.vertx;

/**
 * A REST request context contains information describing the request currently being serviced.
 */
public class MacheRestRequestContext {

    private String mapName;

    public MacheRestRequestContext(String mapName) {
        this.mapName = mapName;
    }

    public String getMapName() {
        return mapName;
    }
}
