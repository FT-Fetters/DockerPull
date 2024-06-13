package com.heybcat.docker.pull.core;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fetters
 */
public class DockerPull {

    private static final String IMG_TAG_SPLIT = ":";

    private static final String LATEST = "latest";

    private static final int SUCCESS_CODE = 200;

    private static final Logger log = LoggerFactory.getLogger(DockerPull.class);
    private static final String DIGEST_FLAG = "digest";
    public static final String DOWNLOAD_TMP = "/download_tmp/";

    private DockerPull() {

    }

    public static void pull(String image, String proxyUrl, Integer proxyPort)
        throws InterruptedException, URISyntaxException, IOException {
        if (!image.contains("/")){
            image = "library/" + image;
        }
        String token = DockerAuth.token(image, proxyUrl, proxyPort);
        if (token == null || token.isEmpty()) {
            return;
        }
        JSONObject manifestInfo;
        if (!image.contains(IMG_TAG_SPLIT)) {
            image = image + IMG_TAG_SPLIT + LATEST;
        }
        String[] split = image.split(IMG_TAG_SPLIT);
        manifestInfo = getImageMainManifest(split[0], split[1], proxyUrl, proxyPort, token,
            null);

        JSONObject config = getConfig(manifestInfo, image, proxyUrl, proxyPort, token);

        JSONArray layers = config.getJSONArray("layers");
        for (int i = 0; i < layers.size(); i++) {
            JSONObject layer = (JSONObject) layers.get(i);
            downloadLayerStream(proxyUrl, proxyPort, token, image, layer.getString(DIGEST_FLAG),
                layers.size(), i);
        }
        ImageTar.downloadAndCreateDockerImageTar(image, layers, config, proxyUrl, proxyPort, token);

    }

    private static JSONObject getConfig(JSONObject manifestInfo, String image, String proxyUrl, Integer proxyPort, String token)
        throws IOException, InterruptedException {
        JSONObject config;
        if (manifestInfo.getJSONArray("manifests") != null){
            JSONArray manifests = manifestInfo.getJSONArray("manifests");
            log.info("choose one");
            for (int i = 0; i < manifests.size(); i++) {
                JSONObject manifest = (JSONObject) manifests.get(i);
                JSONObject platform = manifest.getJSONObject("platform");
                String logInfo = String.format("[%d] architecture: %s, os: %s, size: %d",
                    i, platform.getString("architecture"), platform.getString("os"),
                    manifest.getLong("size"));
                log.info(logInfo);
            }
            int choose = -1;
            Scanner scanner = new Scanner(System.in);
            while (choose == -1) {
                String line = scanner.nextLine();
                int input = Integer.parseInt(line);
                if (input >= manifests.size() || input < 0) {
                    log.info("choose again");
                } else {
                    choose = input;
                }
            }
            JSONObject manifest = (JSONObject) manifests.get(choose);

            log.info("get manifest config...");
            if (!image.contains(IMG_TAG_SPLIT)) {
                config = getImageMainManifest(image, manifest.getString(DIGEST_FLAG), proxyUrl, proxyPort,
                    token,
                    manifest.getString("mediaType"));
            } else {
                String[] split = image.split(IMG_TAG_SPLIT);
                config = getImageMainManifest(split[0], manifest.getString(DIGEST_FLAG), proxyUrl,
                    proxyPort, token,
                    manifest.getString("mediaType"));
            }
            log.info("done");
            return config;
        }else {
            return manifestInfo;
        }


    }

    private static JSONObject getImageMainManifest(String imageName, String reference,
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
                String.format("https://registry-1.docker.io/v2/%s/manifests/%s", imageName,
                    reference)))
            .build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return JSONObject.parseObject(response.body());
    }

    private static void downloadLayerStream(String proxyUrl, Integer proxyPort, String token,
        String image, String layerDigest, int all, int cur)
        throws IOException, InterruptedException {
        String imageName = image.contains(IMG_TAG_SPLIT) ? image.split(IMG_TAG_SPLIT)[0] : image;
        HttpClient httpClient = ClientBuilder.build(proxyUrl, proxyPort);
        HttpRequest request = HttpRequest.newBuilder()
            .header("Authorization", "Bearer " + token)
            .uri(URI.create(
                String.format("https://registry-1.docker.io/v2/%s/blobs/%s", imageName,
                    layerDigest)))
            .build();

        HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
        if (response.statusCode() == SUCCESS_CODE) {
            HttpHeaders headers = response.headers();
            long contentLength = headers.firstValueAsLong("Content-Length").orElse(-1);
            if (contentLength == -1) {
                log.error("stream content length -1");
                return;
            }
            Path path = Paths.get(
                new File("").getAbsolutePath() + DOWNLOAD_TMP + layerDigest.split(IMG_TAG_SPLIT)[1]
                    + ".tar");
            File tarFile = path.toFile();
            if (tarFile.exists() && tarFile.length() == contentLength) {
                log.info("{}.tar file exist, use local file", layerDigest.split(IMG_TAG_SPLIT)[1]);
                return;
            }
            Files.createDirectories(path.getParent());
            try (InputStream inputStream = response.body();
                OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                byte[] buffer = new byte[8192];
                long bytesReadTotal = 0;
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesReadTotal += bytesRead;

                    // 计算并显示下载进度
                    double progress = (double) bytesReadTotal / contentLength * 100;
                    if ((int) progress % 5 == 0) {
                        logProgress(progress, all, cur);
                    }
                }
                log.info("File downloaded successfully to {}", path.toAbsolutePath());
            }
        }
    }

    private static void logProgress(double progress, int all, int cur) {
        // log progress like: [#####     ]
        int progressBarWidth = 20;
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < progressBarWidth; i++) {
            if (i < (int) (progress / 100 * progressBarWidth)) {
                progressBar.append("#");
            } else {
                progressBar.append(" ");
            }
        }
        String progressFormat = String.format("%.2f", progress);
        log.info("[{}] {}% {}/{}", progressBar, progressFormat, cur + 1, all);
    }



}
