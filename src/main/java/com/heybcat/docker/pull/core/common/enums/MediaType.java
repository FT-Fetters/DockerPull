package com.heybcat.docker.pull.core.common.enums;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import java.lang.reflect.Type;
import lombok.Getter;

/**
 * @author Fetters
 */

@Getter
public enum MediaType {

    /**
     * image index v1
     */
    IMAGE_INDEX_V1("application/vnd.oci.image.index.v1+json"),

    IMAGE_MANIFEST_V1_JSON("application/vnd.oci.image.manifest.v1+json"),

    DISTRIBUTION_MANIFEST_V2_JSON("application/vnd.docker.distribution.manifest.v2+json"),

    IMAGE_CONFIG_V1_JSON("application/vnd.oci.image.config.v1+json"),

    IMAGE_LAYER_V1_GZIPPED("application/vnd.oci.image.layer.v1.tar+gzip"),

    IMAGE_LAYER_V1_TAR("application/vnd.oci.image.layer.v1.tar"),
    IMAGE_ROOTFS_DIFF_GZIPPED("application/vnd.docker.image.rootfs.diff.tar.gzip");

    static {
        JSON.register(MediaType.class, MediaTypeReader.INSTANCE);
        JSON.register(MediaType.class, MediaTypeWriter.INSTANCE);
    }

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    public static MediaType getMediaType(String value) {
        for (MediaType mediaType : MediaType.values()) {
            if (mediaType.getValue().equals(value)) {
                return mediaType;
            }
        }
        return null;
    }

    public static class MediaTypeReader implements ObjectReader<MediaType>{

        public static final MediaTypeReader INSTANCE = new MediaTypeReader();

        @Override
        public MediaType readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            return MediaType.getMediaType(jsonReader.readString());
        }
    }

    public static class MediaTypeWriter implements ObjectWriter<MediaType> {

        public static final MediaTypeWriter INSTANCE = new MediaTypeWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString(object.toString());
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
