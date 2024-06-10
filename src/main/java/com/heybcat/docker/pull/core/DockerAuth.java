package com.heybcat.docker.pull.core;

import com.alibaba.fastjson2.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fetters
 */
public class DockerAuth {

    static final Logger log = LoggerFactory.getLogger(DockerAuth.class);

    private static final String SERVICE = "registry.docker.io";
    private static final String AUTH_URL = "https://auth.docker.io/token";

    private static final int SUCCESS_CODE = 200;
    private static final String IMG_TAG_SPLIT = ":";



    private DockerAuth(){
        throw new UnsupportedOperationException();
    }

    public static String token(String image, String proxyUrl, Integer proxyPort)
        throws URISyntaxException, IOException, InterruptedException {
        if (image == null || image.isEmpty()){
            log.info("image is null");
            return null;
        }

        HttpClient httpClient = ClientBuilder.build(proxyUrl, proxyPort);

        URI uri = new URI(buildAuthUrl(image));

        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() != SUCCESS_CODE){
            log.info("auth fail");
            return null;
        }else {
            return JSONObject.parseObject(response.body()).getString("token");
        }

    }


    private static String buildAuthUrl(String image){
        if (image.contains(IMG_TAG_SPLIT)){
            image = image.split(IMG_TAG_SPLIT)[0];
        }
        return AUTH_URL + "?service=" + SERVICE + "&scope=repository:" + image + ":pull";
    }

}
