package com.heybcat.docker.pull.core.async;

import com.heybcat.docker.pull.core.common.Constant;
import com.heybcat.docker.pull.core.common.ImageInfo;
import com.heybcat.docker.pull.core.exception.ImagePullException;
import com.heybcat.docker.pull.core.log.IPullLogger;
import com.heybcat.docker.pull.core.registry.RegistryRestFactory;
import com.heybcat.docker.pull.util.HttpUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * @author Fetters
 */
@Data
public class AsyncBlob implements Runnable {

    private volatile boolean done;

    private volatile long total;

    private AtomicLong current;

    private volatile File file;
    
    private volatile String errorMessage;

    @Getter(AccessLevel.NONE)
    private final String digest;
    @Getter(AccessLevel.NONE)
    private final ImageInfo imageInfo;
    @Getter(AccessLevel.NONE)
    private String token;
    @Getter(AccessLevel.NONE)
    private final String suffix;

    public AsyncBlob(ImageInfo imageInfo, String digest, String token, String suffix){
        this.imageInfo = imageInfo;
        this.digest = digest;
        this.token = token;
        this.suffix = suffix;
    }

    @Override
    public void run() {
        HttpClient httpClient = HttpUtil.buildHttpClient(imageInfo);
        HttpRequest blobRequest = RegistryRestFactory.blobApi(imageInfo.getService(), imageInfo.getFullImageName(), digest, token);
        try {
            HttpResponse<InputStream> resp = httpClient.send(blobRequest, BodyHandlers.ofInputStream());
            if (resp.statusCode() != Constant.STATUS_OK){
                errorMessage = "HTTP request failed with status code: " + resp.statusCode();
                done = true;
                return;
            }
            HttpHeaders headers = resp.headers();
            long contentLength = headers.firstValueAsLong("Content-Length").orElse(-1);
            if (contentLength == -1) {
                errorMessage = "Content-Length header is missing from response";
                done = true;
                return;
            }
            total = contentLength;
            current = new AtomicLong(0);
            if (noNeedDownload(total)){

                current.set(total);
                done = true;
                this.file = getFile();
                return;
            }

            var filePath = getFile().toPath();
            Files.createDirectories(getFile().getParentFile().toPath());

            try (InputStream inputStream = resp.body();
                OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    current.set(current.get() + bytesRead);
                }
            }
            file = getFile();
            done = true;
        } catch (IOException | InterruptedException e) {
            errorMessage = "Download failed: " + e.getMessage();
            Thread.currentThread().interrupt();
            done = true;
        }

    }

    private File getFile(){
        return new File(String.format("%s/download_tmp/%s/%s.%s",
            new File("").getAbsolutePath(), imageInfo.getFullImageName(), digest, suffix)
        );
    }

    public File getBlobFile(){
        return file;
    }
    
    public boolean hasError(){
        return errorMessage != null;
    }

    public void waitForDone(Integer maxWaitTime, IPullLogger log){
        if (maxWaitTime == null) {
            maxWaitTime = Integer.MAX_VALUE;
        }
        int currentWaitTime = 0;
        while (!done) {
            try {
                if (log != null && current != null){
                    log.downloadProgress("layer#" + digest.substring(7, 16) + ">> ", current.get(), total);
                }
                Thread.sleep(100);
                currentWaitTime += 1;
                if (currentWaitTime > maxWaitTime) {
                    throw new ImagePullException("Timeout waiting for download to complete");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ImagePullException("Interrupted while waiting for download to complete");
            }
        }
        if (log != null && current != null){
            log.downloadProgress("layer#" + digest.substring(7, 16) + ">> ", current.get(), total);
        }
    }

    private boolean noNeedDownload(long total) {
        return getFile().exists() && total == getFile().length();
    }
}
