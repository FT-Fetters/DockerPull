package com.heybcat.docker.pull.core;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fetters
 */
public class ImageTar {

    private static final String IMG_TAG_SPLIT = ":";

    private static final int SUCCESS_CODE = 200;

    private static final Logger log = LoggerFactory.getLogger(ImageTar.class);
    private static final String DIGEST_FLAG = "digest";
    public static final String DOWNLOAD_TMP = "/download_tmp/";

    private ImageTar(){

    }

    public static void downloadAndCreateDockerImageTar(String imageName, JSONArray layers,
        JSONObject config, String proxyUrl, Integer proxyPort, String token)
        throws IOException, InterruptedException {
        // 下载配置文件
        String configDigest = config.getJSONObject("config").getString(DIGEST_FLAG);
        downloadConfigFile(imageName, configDigest, proxyUrl, proxyPort, token);

        Path tarFilePath = Paths.get(
            new File("").getAbsolutePath() + "/images/" + imageName.
                replace("/", "_").replace(IMG_TAG_SPLIT, "_") + ".tar.gz");
        Files.createDirectories(tarFilePath.getParent());

        try (OutputStream fo = Files.newOutputStream(tarFilePath);
            BufferedOutputStream bo = new BufferedOutputStream(fo);
            GZIPOutputStream go = new GZIPOutputStream(bo);
            TarArchiveOutputStream to = new TarArchiveOutputStream(go)) {

            // 添加config.json
            Path configPath = Paths.get(
                new File("").getAbsolutePath() + DOWNLOAD_TMP + configDigest.split(IMG_TAG_SPLIT)[1]
                    + ".json");
            addFileToTar(to, configPath.toFile(), configDigest.split(IMG_TAG_SPLIT)[1] + ".json");
            log.info("add {}.json to tar file", configDigest.split(IMG_TAG_SPLIT)[1]);

            // 添加manifest.json
            String manifestJson = createManifestJson(imageName, layers, configDigest);
            Path manifestPath = Paths.get("manifest.json");
            Files.writeString(manifestPath, manifestJson);
            addFileToTar(to, manifestPath.toFile(), "manifest.json");
            log.info("add manifest.json to tar file");

            // 添加repositories
            String repositoriesContent = createRepositoriesContent(imageName);
            Path repositoriesPath = Paths.get("repositories");
            Files.writeString(repositoriesPath, repositoriesContent);
            addFileToTar(to, repositoriesPath.toFile(), "repositories");
            log.info("add repositories to tar file");

            // 添加层文件
            for (Object o : layers) {
                JSONObject layer = (JSONObject) o;
                Path layerPath = Paths.get(
                    new File("").getAbsolutePath() + DOWNLOAD_TMP + layer.getString(DIGEST_FLAG)
                        .split(IMG_TAG_SPLIT)[1] + ".tar");
                if (Files.exists(layerPath)) {
                    addFileToTar(to, layerPath.toFile(),
                        layer.getString(DIGEST_FLAG).split(IMG_TAG_SPLIT)[1] + "/layer.tar");
                    log.info("add {} layer.tar to tar file", layer.getString(DIGEST_FLAG));
                }
            }

            to.finish();
            log.info("Docker image tar created successfully at {}", tarFilePath.toAbsolutePath());
        }
    }


    private static void addFileToTar(TarArchiveOutputStream to, File file, String entryName)
        throws IOException {
        TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
        to.putArchiveEntry(tarEntry);
        Files.copy(file.toPath(), to);
        to.closeArchiveEntry();
    }

    private static String createManifestJson(String image, JSONArray layers, String configDigest) {
        JSONObject manifestJson = new JSONObject();
        manifestJson.put("Config", configDigest.split(IMG_TAG_SPLIT)[1] + ".json");
        manifestJson.put("RepoTags", Collections.singletonList(image));
        manifestJson.put("Layers", layers.stream().map(layer -> {
            JSONObject jsonObject = (JSONObject) layer;
            return jsonObject.getString(DIGEST_FLAG).split(IMG_TAG_SPLIT)[1] + "/layer.tar";
        }).collect(Collectors.toList()));
        JSONArray arr = new JSONArray();
        arr.add(manifestJson);
        return arr.toJSONString();
    }

    private static String createRepositoriesContent(String imageName) {
        // 构建repositories文件内容
        // 返回字符串
        JSONObject repositories = new JSONObject();
//        JSONObject body = new JSONObject();
//        body.put(imageName.split(IMG_TAG_SPLIT)[0],
//            imageName.split(IMG_TAG_SPLIT)[1]);
        repositories.put(imageName.split(IMG_TAG_SPLIT)[0], imageName.split(IMG_TAG_SPLIT)[1]);
        return repositories.toJSONString();
    }

    private static void downloadConfigFile(String imageName, String configDigest, String proxyUrl,
        Integer proxyPort, String token) throws IOException, InterruptedException {
        // 实现配置文件的下载逻辑
        // 将下载的配置文件保存到download_tmp目录
        imageName =
            imageName.contains(IMG_TAG_SPLIT) ? imageName.split(IMG_TAG_SPLIT)[0] : imageName;
        String url = "https://registry-1.docker.io/v2/" + imageName + "/blobs/" + configDigest;
        HttpClient httpClient = ClientBuilder.build(proxyUrl, proxyPort);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
            .header("Authorization", "Bearer " + token).build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() != SUCCESS_CODE) {
            log.error("Failed to download config file");
            return;
        }
        Path path = Paths.get(
            new File("").getAbsolutePath() + DOWNLOAD_TMP + configDigest.split(IMG_TAG_SPLIT)[1]
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

}
