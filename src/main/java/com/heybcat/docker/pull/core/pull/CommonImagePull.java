package com.heybcat.docker.pull.core.pull;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.heybcat.docker.pull.core.ClientBuilder;
import com.heybcat.docker.pull.core.DockerAuth;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonImagePull {

    private static final String IMG_TAG_SPLIT = ":";

    private static final String LATEST = "latest";

    private static final int SUCCESS_CODE = 200;

    private static final Logger log = LoggerFactory.getLogger(CommonImagePull.class);
    private static final String DIGEST_FLAG = "digest";
    public static final String DOWNLOAD_TMP = "/download_tmp/";

    public static void pull(String service, String namespace, String image, String tag, String proxyUrl, Integer proxyPort)
        throws URISyntaxException, IOException, InterruptedException {
        String token = DockerAuth.token(service, namespace + "/" + image, proxyUrl, proxyPort);
        JSONObject imageMainManifest = getImageMainManifest(service, namespace + "/" + image, tag, proxyUrl,
            proxyPort, token, null);
        JSONArray manifests = imageMainManifest.getJSONArray("manifests");
        JSONObject manifest = manifests.getJSONObject(0);
        JSONObject config;
        if (!image.contains(IMG_TAG_SPLIT)) {
            config = getImageMainManifest(service,namespace + "/" + image, manifest.getString(DIGEST_FLAG), proxyUrl, proxyPort,
                token,
                manifest.getString("mediaType"));
        } else {
            String[] split = image.split(IMG_TAG_SPLIT);
            config = getImageMainManifest(service, namespace + "/" + split[0], manifest.getString(DIGEST_FLAG), proxyUrl,
                proxyPort, token,
                manifest.getString("mediaType"));
        }
        System.out.println(config);
        System.out.println(imageMainManifest);

    }

    private static JSONObject getImageMainManifest(String service, String imageName, String reference,
        String proxyUrl, Integer proxyPort,
        String token, String mediaType)
        throws IOException, InterruptedException {
        HttpClient httpClient = ClientBuilder.build(proxyUrl, proxyPort);

        if (mediaType == null) {
            mediaType = "application/vnd.oci.image.index.v1+json, application/vnd.docker.distribution.manifest.v2+json";
        }
        // https://registry-1.docker.io/v2/library/bash/manifests/devel
        HttpRequest request = HttpRequest.newBuilder()
            .header("Authorization", "Bearer " + token)
            .header("Accept", mediaType)
            .uri(URI.create(
                String.format("https://"+ service +"/v2/%s/manifests/%s", imageName,
                    reference)))
            .build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return JSONObject.parseObject(response.body());
    }

}
