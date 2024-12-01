package com.heybcat.docker.pull.core.common.oci;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.heybcat.docker.pull.core.common.enums.MediaType;
import com.heybcat.docker.pull.core.common.enums.MediaType.MediaTypeReader;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Fetters
 */
@Setter
@Getter
public class Manifests {



    @JSONField(name = "manifests")
    private List<Manifest> manifestList;

    private Integer schemaVersion;

    private MediaType mediaType;

    private Config config;

    private List<Layer> layers;

    public static Manifests parse(String json) {
        JSON.register(MediaType.class, MediaTypeReader.INSTANCE);
        return JSON.parseObject(json, Manifests.class);

    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }
}
