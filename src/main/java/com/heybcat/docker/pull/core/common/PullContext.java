package com.heybcat.docker.pull.core.common;

import com.heybcat.docker.pull.core.common.oci.Manifest;
import com.heybcat.docker.pull.core.common.oci.Manifests;
import java.io.File;
import java.util.List;
import lombok.Data;

/**
 * @author Fetters
 */
@Data
public class PullContext {

    private ImageInfo imageInfo;

    private String token;

    private Manifests manifests;

    private Manifest chosenManifest;

    private Manifests specifyManifest;

    private List<File> layerFileList;

    private File configFile;

    private File imageTarFile;
}
