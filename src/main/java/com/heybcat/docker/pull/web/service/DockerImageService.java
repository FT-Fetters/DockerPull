package com.heybcat.docker.pull.web.service;

import com.heybcat.docker.pull.core.pull.BaseDockerPull;
import com.heybcat.docker.pull.core.sftp.SftpUploader;
import com.heybcat.docker.pull.session.SessionManager;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.docker.pull.web.entity.view.LocalImagesView;
import com.heybcat.docker.pull.web.entity.view.LocalImagesView.LocalImage;
import com.heybcat.docker.pull.web.entity.view.UploadImageView;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.common.util.FileUtil;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
@Cat
public class DockerImageService {

    private final ThreadPoolExecutor threadPoolExecutor;

    public DockerImageService() {
        this.threadPoolExecutor = new ThreadPoolExecutor(
            8, 8, 0L, java.util.concurrent.TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), r -> new Thread(r, "docker-pull-" + r.hashCode()));
    }

    public String pull(String namespace, String image, String tag, String os, String arch) {
        String session = SessionManager.getInstance().newSession();
        SessionManager.getInstance().changeStatus(session, "waiting");
        threadPoolExecutor.execute(
            () -> BaseDockerPull.pull(null, namespace, image, tag, os, arch,
                GlobalConfig.getProxyHost(),
                GlobalConfig.getProxyPort() != null ? Integer.valueOf(GlobalConfig.getProxyPort())
                    : null, session)
        );
        return session;
    }

    public LocalImagesView localImages(Integer cur, Integer size, String keyword, String order, String orderBy) {
        LocalImagesView localImagesView = new LocalImagesView();
        Path imagesPath = Paths.get(new File("").getAbsolutePath() + "/images/");
        if (!imagesPath.toFile().exists()){
            localImagesView.setTotal(0);
            return localImagesView;
        }
        List<File> files = FileUtil.listAllFilesInDir(imagesPath.toAbsolutePath().toString());
        if (StringUtil.isNotBlank(keyword)) {
            files = files.stream().filter(file -> file.getName().contains(keyword)).collect(Collectors.toList());
        }
        localImagesView.setTotal(files.size());
        files = sortImageFiles(order, orderBy, files);
        if (cur != null && size != null) {
            files = files.stream().skip((long) (cur - 1) * size).limit(size).collect(Collectors.toList());
        }
        localImagesView.setCur(cur);
        localImagesView.setImages(new ArrayList<>());
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File file : files) {
            LocalImage localImage = new LocalImage();
            localImage.setFileName(file.getName());
            localImage.setSize(file.length());
            localImage.setCreatedTime(dateTimeFormatter.format(new Date(file.lastModified())));
            localImagesView.getImages().add(localImage);
        }
        return localImagesView;
    }

    private static List<File> sortImageFiles(String order, String orderBy, List<File> files) {
        if (!StringUtil.isNotBlank(order) || !StringUtil.isNotBlank(orderBy)) {
            return files;
        }
        if ("size".equals(orderBy)) {
            files = files.stream().sorted((o1, o2) -> {
                if ("ASC".equals(order)) {
                    return Math.toIntExact(o1.length() - o2.length());
                } else {
                    return Math.toIntExact(o2.length() - o1.length());
                }
            }).collect(Collectors.toList());
        } else if ("time".equals(orderBy)) {
            files = files.stream().sorted((o1, o2) -> {
                if ("ASC".equals(order)) {
                    return Math.toIntExact(o1.lastModified() - o2.lastModified());
                } else {
                    return Math.toIntExact(o2.lastModified() - o1.lastModified());
                }
            }).collect(Collectors.toList());
        }
        return files;
    }

    public String deleteImage(String fileName) {
        Path imagesPath = Paths.get(new File("").getAbsolutePath() + "/images/");
        File targetFile = new File(imagesPath.toAbsolutePath().toString(), fileName);
        if (targetFile.exists()) {
            try {
                Files.delete(targetFile.toPath());
                return "deleted";
            } catch (Exception e) {
                return "delete failed, " + e.getMessage();
            }
        } else {
            return "file not found";
        }
    }

    public UploadImageView uploadImage(String fileName) {
        Path imagesPath = Paths.get(new File("").getAbsolutePath() + "/images/");
        File targetFile = new File(imagesPath.toAbsolutePath().toString(), fileName);
        if (!targetFile.exists()) {
            return UploadImageView.fail("file not found");
        }

        return SftpUploader.upload(targetFile, GlobalConfig.getSshSavePath());

    }
}
