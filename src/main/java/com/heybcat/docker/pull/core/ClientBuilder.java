package com.heybcat.docker.pull.core;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;

/**
 * @author Fetters
 */
public class ClientBuilder {

    private ClientBuilder(){
    }

    public static HttpClient build(String proxyUrl, Integer proxyPort){
        Builder builder = HttpClient.newBuilder();
        if (proxyUrl != null && !proxyUrl.isEmpty() && proxyPort != null){
            ProxySelector proxySelector = ProxySelector.of(
                new InetSocketAddress(proxyUrl, proxyPort));
            builder.proxy(proxySelector);
        }
        builder.followRedirects(HttpClient.Redirect.NORMAL);
        return builder.build();
    }

}
