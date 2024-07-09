package com.heybcat.docker.pull.core;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class ImagePackager {

    private static final String IMG_TAG_SPLIT = ":";


    private static final Logger log = LoggerFactory.getLogger(ImagePackager.class);
    private static final String DIGEST_FLAG = "digest";
    public static final String DOWNLOAD_TMP = "/download_tmp/";

    public static void packImage(String namespace, String image, String tag, JSONArray layers,
        JSONObject config) throws IOException {

        String configDigest = config.getJSONObject("config").getString(DIGEST_FLAG);

        Path tarFilePath = Paths.get(
            new File("").getAbsolutePath() + "/images/" + namespace + "_" + image + "_" + tag + ".tar.gz");
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
            String manifestJson = createManifestJson(namespace, image, tag, layers, configDigest);
            Path manifestPath = Paths.get("manifest.json");
            Files.writeString(manifestPath, manifestJson);
            addFileToTar(to, manifestPath.toFile(), "manifest.json");
            log.info("add manifest.json to tar file");

            // 添加repositories
            String repositoriesContent = createRepositoriesContent(namespace, image, tag);
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

    private static String createManifestJson(String namespace, String image, String tag, JSONArray layers, String configDigest) {
        JSONObject manifestJson = new JSONObject();
        manifestJson.put("Config", configDigest.split(IMG_TAG_SPLIT)[1] + ".json");
        manifestJson.put("RepoTags", Collections.singletonList(namespace + "/" + image + ":" + tag));
        manifestJson.put("Layers", layers.stream().map(layer -> {
            JSONObject jsonObject = (JSONObject) layer;
            return jsonObject.getString(DIGEST_FLAG).split(IMG_TAG_SPLIT)[1] + "/layer.tar";
        }).collect(Collectors.toList()));
        JSONArray arr = new JSONArray();
        arr.add(manifestJson);
        return arr.toJSONString();
    }

    private static String createRepositoriesContent(String namespace, String image, String tag) {
        // 构建repositories文件内容
        // 返回字符串
        JSONObject repositories = new JSONObject();
        repositories.put(namespace + "/" + image, tag);
        return repositories.toJSONString();
    }


}
