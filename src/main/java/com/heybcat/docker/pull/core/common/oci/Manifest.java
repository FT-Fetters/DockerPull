package com.heybcat.docker.pull.core.common.oci;

import com.heybcat.docker.pull.core.common.enums.MediaType;
import java.util.Map;

/**
 * @author Fetters
 */
public class Manifest {

    private String digest;

    private MediaType mediaType;

    private Integer size;

    private Platform platform;

    private Map<String, String> annotations;

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }
}
