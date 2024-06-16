package com.heybcat.docker.pull.web.config;

/**
 * @author Fetters
 */
public class GlobalConfig {

    private static String proxyHost;

    private static String proxyPort;

    public static String getProxyHost() {
        return proxyHost;
    }

    public static void setProxyHost(String proxyHost) {
        GlobalConfig.proxyHost = proxyHost;
    }

    public static String getProxyPort() {
        return proxyPort;
    }

    public static void setProxyPort(String proxyPort) {
        GlobalConfig.proxyPort = proxyPort;
    }
}
