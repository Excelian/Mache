package com.excelian.mache.rest;

/**
 * Options used to start the REST service.
 */
public class MacheRestServiceConfiguration {
    private int bindPort = 8080;
    private String bindIp = "localhost";
    private boolean localOnly = true;

    /**
     * Creates a default configuration.
     */
    public MacheRestServiceConfiguration() {
    }

    /**
     * Creates a service configuration with the given settings.
     *
     * @param bindPort  The port to bind on
     * @param bindIp    The address to bind on
     * @param localOnly Indicates if only loopback connections are allowed to this instance
     */
    public MacheRestServiceConfiguration(int bindPort, String bindIp, boolean localOnly) {
        this.bindPort = bindPort;
        this.bindIp = bindIp;
        this.localOnly = localOnly;
    }

    public int getBindPort() {
        return bindPort;
    }

    public String getBindIp() {
        return bindIp;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }
}
