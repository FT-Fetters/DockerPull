package com.heybcat.docker.pull.core;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Version;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fetters
 */
public class ClientBuilder {

    private static final Map<String, HttpClient> CLIENT_CACHE = new HashMap<>();

    private ClientBuilder(){
    }

    public static HttpClient build(String proxyUrl, Integer proxyPort){
        if (CLIENT_CACHE.get(proxyUrl + proxyPort) != null){
            return CLIENT_CACHE.get(proxyUrl + proxyPort);
        }
        Builder builder = HttpClient.newBuilder();
        if (proxyUrl != null && !proxyUrl.isEmpty() && proxyPort != null){
            ProxySelector proxySelector = ProxySelector.of(
                new InetSocketAddress(proxyUrl, proxyPort));
            builder.proxy(proxySelector);
        }
        builder.version(Version.HTTP_1_1);
        builder.followRedirects(HttpClient.Redirect.NORMAL);
        HttpClient client = builder.build();
        CLIENT_CACHE.put(proxyUrl + proxyPort, client);
        return client;
    }

}
