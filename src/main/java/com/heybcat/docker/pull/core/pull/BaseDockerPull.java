package com.heybcat.docker.pull.core.pull;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.heybcat.docker.pull.core.ClientBuilder;
import com.heybcat.docker.pull.core.DockerAuth;
import com.heybcat.docker.pull.core.ImagePackager;
import com.heybcat.docker.pull.session.PullSessionManager;
import com.heybcat.docker.pull.web.entity.PullResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class BaseDockerPull {

    private static final String DOWNLOAD_TMP = "/download_tmp/";

    private static final String DEFAULT_BASE_URL = "https://registry-1.docker.io";

    private static final String DEFAULT_NAMESPACE = "library";

    private static final String DEFAULT_TAG = "latest";

    private static final String SPLIT_CHAR = "/";

    private static final String ERROR_FLAG = "errors";
    private static final Logger log = LoggerFactory.getLogger(BaseDockerPull.class);

    private BaseDockerPull() {
        throw new IllegalStateException();
    }

    public static PullResult pull(
        String baseUrl,
        String namespace,
        String image,
        String tag,
        String os,
        String arch,
        String proxyUrl,
        Integer proxyPort,
        String session
    ) {
        baseUrl = baseUrl == null ? DEFAULT_BASE_URL : baseUrl;
        namespace = namespace == null ? DEFAULT_NAMESPACE : namespace;
        tag = tag == null ? DEFAULT_TAG : tag;
        if (StringUtil.isBlank(image)) {
            return new PullResult(false, "image is empty");
        }

        if (baseUrl.equals(DEFAULT_BASE_URL)) {
            return pullByDefault(namespace, image, tag, os, arch, proxyUrl, proxyPort,
                session);
        }
        return null;

    }

    private static PullResult pullByDefault(
        String namespace,
        String image,
        String tag,
        String os,
        String arch,
        String proxyUrl,
        Integer proxyPort,
        String session
    ) {

        try {
            String token = DockerAuth.token(namespace + SPLIT_CHAR + image, proxyUrl, proxyPort);

            PullSessionManager.getInstance().changeStatus(session, "waiting");
            JSONObject manifests = getImageMainManifestByDefault(namespace, image, tag,
                proxyUrl, proxyPort, token, null);
            if (manifests.containsKey(ERROR_FLAG)) {
                return new PullResult(false, manifests.getJSONArray(ERROR_FLAG).toJSONString());
            }

            PullSessionManager.getInstance().changeStatus(session, "find_config");
            JSONObject config =
                manifests.containsKey("manifests") ? getConfig(manifests, namespace, image,
                    proxyUrl, proxyPort, token, os, arch) : manifests;
            PullSessionManager.getInstance().changeStatus(session, "download_config");

            String configDigest = config.getJSONObject("config").getString("digest");

            downloadConfigFile(namespace, image, configDigest, proxyUrl, proxyPort, token);

            JSONArray layers = config.getJSONArray("layers");

            int totalSize = getTotalSize(layers);
            downloadLayers(proxyUrl, proxyPort, token, namespace, image, session, layers, totalSize);
            PullSessionManager.getInstance().changeStatus(session, "package_image");
            ImagePackager.packImage(namespace, image, tag, layers, config);
            PullSessionManager.getInstance().changeStatus(session, "finished");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new PullResult(false, e.getMessage());
        }
        return null;

    }

    private static JSONObject getImageMainManifestByDefault(String namespace, String image,
        String tag,
        String proxyUrl, Integer proxyPort,
        String token, String mediaType)
        throws IOException, InterruptedException {
        HttpClient httpClient = ClientBuilder.build(proxyUrl, proxyPort);

        if (mediaType == null) {
            mediaType = "application/vnd.docker.distribution.manifest.v2+json";
        }
        HttpRequest request = HttpRequest.newBuilder()
            .header("Authorization", "Bearer " + token)
            .header("Accept", mediaType)
            .uri(URI.create(
                String.format("https://registry-1.docker.io/v2/%s/manifests/%s",
                    namespace + SPLIT_CHAR + image,
                    tag)))
            .build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return JSONObject.parseObject(response.body());
    }

    private static JSONObject getConfig(JSONObject manifests, String namespace, String image,
        String proxyUrl, Integer proxyPort, String token, String os, String arch)
        throws IOException, InterruptedException {
        JSONArray manifestsArray = manifests.getJSONArray("manifests");
        for (int i = 0; i < manifestsArray.size(); i++) {
            JSONObject manifest = manifestsArray.getJSONObject(i);
            JSONObject platform = manifest.getJSONObject("platform");
            if (platform.getString("os").equals(os) && platform.getString("architecture")
                .equals(arch)) {
                return getImageMainManifestByDefault(namespace, image, manifest.getString("digest"),
                    proxyUrl, proxyPort,
                    token, manifest.getString("mediaType"));
            }
        }
        return null;
    }

    private static int getTotalSize(JSONArray layers) {
        int totalSize = 0;
        for (int i = 0; i < layers.size(); i++) {
            JSONObject layer = layers.getJSONObject(i);
            totalSize += layer.getInteger("size");
        }
        return totalSize;
    }

    private static void downloadConfigFile(String namespace, String image, String configDigest,
        String proxyUrl,
        Integer proxyPort, String token) throws IOException, InterruptedException {
        // 实现配置文件的下载逻辑
        // 将下载的配置文件保存到download_tmp目录
        String url = "https://registry-1.docker.io/v2/" + namespace + SPLIT_CHAR + image + "/blobs/"
            + configDigest;
        HttpClient httpClient = ClientBuilder.build(proxyUrl, proxyPort);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
            .header("Authorization", "Bearer " + token).build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.error("Failed to download config file");
            return;
        }
        Path path = Paths.get(
            new File("").getAbsolutePath() + DOWNLOAD_TMP + configDigest.split(":")[1]
                + ".json");
        Files.createDirectories(path.getParent());
        try (InputStream inputStream = new ByteArrayInputStream(response.body().getBytes());
            OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            log.info("Config file downloaded successfully to {}", path.toAbsolutePath());
        }

    }

    private static void downloadLayers(String proxyUrl, Integer proxyPort, String token,
        String namespace, String image, String session, JSONArray layers, long totalSize)
        throws IOException, InterruptedException {
        long bytesReadTotal = 0;
        PullSessionManager.getInstance().changeStatus(session, "download_layers");
        for (int i = 0; i < layers.size(); i++) {
            JSONObject layer = layers.getJSONObject(i);
            String layerDigest = layer.getString("digest");
            HttpClient httpClient = ClientBuilder.build(proxyUrl, proxyPort);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
                    String.format("https://registry-1.docker.io/v2/%s/blobs/%s",
                        namespace + SPLIT_CHAR + image, layerDigest)))
                .header("Authorization", "Bearer " + token).build();

            HttpResponse<InputStream> response = httpClient.send(request,
                BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                HttpHeaders headers = response.headers();
                long contentLength = headers.firstValueAsLong("Content-Length").orElse(-1);
                if (contentLength == -1) {
                    log.error("stream content length -1");
                    return;
                }
                Path path = Paths.get(
                    new File("").getAbsolutePath() + DOWNLOAD_TMP + layerDigest.split(":")[1] + ".tar");
                File tarFile = path.toFile();
                if (tarFile.exists() && tarFile.length() == contentLength) {
                    log.info("{}.tar file exist, use local file", layerDigest.split(":")[1]);
                    bytesReadTotal += contentLength;
                    double progress = (double) bytesReadTotal / totalSize * 100;
                    logProgress(progress, layers.size(), i, totalSize, bytesReadTotal, session);
                    continue;
                }
                Files.createDirectories(path.getParent());
                try (InputStream inputStream = response.body();
                    OutputStream outputStream = Files.newOutputStream(path,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        bytesReadTotal += bytesRead;

                        // 计算并显示下载进度
                        double progress = (double) bytesReadTotal / totalSize * 100;
                        logProgress(progress, layers.size(), i, totalSize, bytesReadTotal, session);
                    }
                    log.info("File downloaded successfully to {}", path.toAbsolutePath());
                }
            }
        }
    }

    private static void logProgress(double progress, int all, int cur, long allBytes, long curBytes, String session) {
        // log progress like: [#####     ]
        int progressBarWidth = 25;
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < progressBarWidth; i++) {
            if (i < (int) (progress / 100 * progressBarWidth)) {
                progressBar.append("#");
            } else {
                progressBar.append(" ");
            }
        }
        String progressFormat = String.format("%.2f", progress);
        PullSessionManager.getInstance().updateProgress(session, progress);
        if (PullSessionManager.getInstance().getSession(session) == null){
            log.info("[{}] {}% {}/{} {}kb/{}kb", progressBar, progressFormat, cur + 1, all, curBytes / 1024, allBytes / 1024);
        }
    }
}
