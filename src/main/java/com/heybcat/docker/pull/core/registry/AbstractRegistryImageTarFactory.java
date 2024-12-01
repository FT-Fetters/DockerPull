package com.heybcat.docker.pull.core.registry;

import com.heybcat.docker.pull.core.common.ImageInfo;
import com.heybcat.docker.pull.core.common.oci.Manifest;
import com.heybcat.docker.pull.core.common.oci.Manifests;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Fetters
 */
public abstract class AbstractRegistryImageTarFactory {

    public abstract AbstractRegistryImageTarFactory imageInfo(ImageInfo imageInfo);

    public abstract AbstractRegistryImageTarFactory layerFileList(List<File> layerFileList);

    public abstract AbstractRegistryImageTarFactory configFile(File configFile);

    public abstract AbstractRegistryImageTarFactory manifests(Manifests manifests);

    public abstract AbstractRegistryImageTarFactory config(Manifests config);

    public abstract AbstractRegistryImageTarFactory manifest(Manifest manifest);

    public abstract File build() throws IOException;

    protected static void parameterVerify(ImageInfo imageInfo, List<File> layerFileList, File configFile, Manifests manifests,
        Manifests config){
        if (imageInfo == null) {
            throw new IllegalArgumentException("imageInfo can not be null");
        }
        if (layerFileList == null) {
            throw new IllegalArgumentException("layerFileList can not be null");
        }
        if (configFile == null) {
            throw new IllegalArgumentException("configFile can not be null");
        }
        if (manifests == null) {
            throw new IllegalArgumentException("manifests can not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config can not be null");
        }
    }
}
