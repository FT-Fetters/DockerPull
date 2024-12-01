package com.heybcat.docker.pull.core.registry;

import com.heybcat.docker.pull.core.DockerAuth;
import com.heybcat.docker.pull.core.async.AsyncBlob;
import com.heybcat.docker.pull.core.common.Constant;
import com.heybcat.docker.pull.core.common.ImageInfo;
import com.heybcat.docker.pull.core.common.enums.MediaType;
import com.heybcat.docker.pull.core.common.oci.Manifests;
import com.heybcat.docker.pull.util.HttpUtil;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Fetters
 */
@Slf4j
public class RegistryHelper {

    private RegistryHelper() {}

    private static final ExecutorService TRANSFER_WORKER = new ThreadPoolExecutor(4, 4, 60,
        TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> new Thread(r, "transferWorker-" + r.hashCode()),
        new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * get pull image token
     * @param imageInfo image info, include namespace, image, tag, os, arch...
     * @return auth token string
     */
    public static String getPullToken(ImageInfo imageInfo){
        String completeImage = imageInfo.getNamespace() + "/" + imageInfo.getImage();
        try {
            return DockerAuth.token(imageInfo.getService(), completeImage, imageInfo.getProxyAddress().getHostName(), imageInfo.getProxyAddress().getPort());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public static Manifests getManifests(ImageInfo imageInfo, String configDigest, String token, MediaType... mediaTypes) throws IOException, InterruptedException {
        HttpClient httpClient = HttpUtil.buildHttpClient(imageInfo);

        String mediaType;
        if (mediaTypes == null || mediaTypes.length == 0){
            mediaType = String.format("%s, %s", MediaType.IMAGE_INDEX_V1.getValue(), MediaType.DISTRIBUTION_MANIFEST_V2_JSON.getValue());
        }else {
           mediaType = Arrays.stream(MediaType.values()).map(MediaType::getValue).collect(Collectors.joining(", "));
        }
        String ref;
        if (StringUtils.isNotBlank(configDigest)){
            ref = configDigest;
        }else {
            ref = StringUtils.isNotBlank(imageInfo.getDigest()) ? imageInfo.getDigest() : imageInfo.getTag();
        }
        HttpRequest request = RegistryRestFactory.manifestsApi(imageInfo.getService(), imageInfo.getFullImageName(), ref, token, mediaType);

        HttpResponse<String> resp = httpClient.send(request, BodyHandlers.ofString());
        if (resp.statusCode() == Constant.STATUS_OK){
            return Manifests.parse(resp.body());
        }else {
            log.error("get manifests error, status code: {}, body: {}", resp.statusCode(), resp.body());
            return null;
        }
    }

    public static AsyncBlob transferBlobs(ImageInfo imageInfo, String digest, String token, String suffix){
        AsyncBlob asyncBlob = new AsyncBlob(imageInfo, digest, token, suffix);
        TRANSFER_WORKER.execute(asyncBlob);
        return asyncBlob;
    }

}
