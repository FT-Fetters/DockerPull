package com.heybcat.docker.pull.core.common.oci;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author Fetters
 */
@Data
public class TarManifest {

    @JSONField(name = "Config")
    private String config;

    @JSONField(name = "Layers")
    private List<String> layers;

    @JSONField(name = "RepoTags")
    private List<String> repoTags;

    @JSONField(name = "LayerSources")
    private Map<String, Layer> layerSources;

    public String toJsonString() {
        return JSON.toJSONString(Collections.singletonList(this));
    }

}
