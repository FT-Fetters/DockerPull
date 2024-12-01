package com.heybcat.docker.pull.core.registry;

import java.net.URI;
import java.net.http.HttpRequest;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class RegistryRestFactory {

    private RegistryRestFactory(){}

    public static HttpRequest manifestsApi(String service, String fullImageName, String digestOrTag, String token, String mediaType){
        if (StringUtil.isBlank(service)){
            service = "registry-1.docker.io";
        }
        return HttpRequest.newBuilder()
            .header("Authorization", "Bearer " + token)
            .header("Accept", mediaType)
            .uri(URI.create(
                    String.format("https://%s/v2/%s/manifests/%s", service, fullImageName, digestOrTag)
                )
            )
            .build();
    }

    public static HttpRequest blobApi(String service, String fullImageName, String digest, String token){
        if (StringUtil.isBlank(service)){
            service = "registry-1.docker.io";
        }
        return HttpRequest.newBuilder()
            .header("Authorization", "Bearer " + token)
            .uri(URI.create(
                    String.format("https://%s/v2/%s/blobs/%s", service, fullImageName, digest)
                )
            )
            .build();
    }

}
