package com.heybcat.docker.pull.core.registry;

import com.heybcat.docker.pull.core.common.Constant;
import com.heybcat.docker.pull.core.common.ImageInfo;
import com.heybcat.docker.pull.core.common.TempDir;
import com.heybcat.docker.pull.core.common.oci.Layer;
import com.heybcat.docker.pull.core.common.oci.Manifest;
import com.heybcat.docker.pull.core.common.oci.Manifests;
import com.heybcat.docker.pull.core.common.oci.TarManifest;
import com.heybcat.docker.pull.core.exception.ImageTarException;
import com.heybcat.docker.pull.util.TarUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * @author Fetters
 */
public class RegistryImageTarFactoryDefault extends AbstractRegistryImageTarFactory {

    private static final String DEFAULT_OCI_LAYOUT_CONTENT = "{\"imageLayoutVersion\": \"1.0.0\"}";

    private ImageInfo imageInfo;

    private List<File> layerFileList;

    private File configFile;

    private Manifests manifests;

    private Manifests specifyManifest;

    private Manifest manifest;

    private RegistryImageTarFactoryDefault(){}

    public static AbstractRegistryImageTarFactory builder() {
        return new RegistryImageTarFactoryDefault();
    }

    @Override
    public AbstractRegistryImageTarFactory imageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
        return this;
    }

    @Override
    public AbstractRegistryImageTarFactory layerFileList(List<File> layerFileList) {
        this.layerFileList = layerFileList;
        return this;
    }

    @Override
    public AbstractRegistryImageTarFactory configFile(File configFile) {
        this.configFile = configFile;
        return this;
    }

    @Override
    public AbstractRegistryImageTarFactory manifests(Manifests manifests) {
        this.manifests = manifests;
        return this;
    }

    @Override
    public AbstractRegistryImageTarFactory config(Manifests config) {
        this.specifyManifest = config;
        return this;
    }

    @Override
    public AbstractRegistryImageTarFactory manifest(Manifest manifest) {
        this.manifest = manifest;
        return this;
    }

    private File outputTarFile() {
        return new File(String.format("%s/images/%s.%s",
            new File("").getAbsolutePath(), imageInfo.getFullImageName(), Constant.TAR_SUFFIX)
        );
    }

    @Override
    public File build() throws IOException {
        // check all parameters first
        parameterVerify(this.imageInfo, this.layerFileList, this.configFile, this.manifests, this.specifyManifest);

        File tarFile = outputTarFile();
        Files.createDirectories(tarFile.getParentFile().toPath());

        try (OutputStream outputStream = Files.newOutputStream(tarFile.toPath());
            BufferedOutputStream bufferedOut = new BufferedOutputStream(outputStream);
            GZIPOutputStream gzipOut = new GZIPOutputStream(bufferedOut);
            TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut);
            TempDir tempDir = new TempDir()) {

            // append index.json file
            File indexFile = buildIndexFile(tempDir);
            TarUtil.appendFileToTar(tarOut, indexFile, "index.json");

            // append manifest.json file
            File manifestFile = buildManifestFile(tempDir);
            TarUtil.appendFileToTar(tarOut, manifestFile, "manifest.json");

            //append oci-layout file
            File ociLayoutFile = buildOciLayoutFile(tempDir);
            TarUtil.appendFileToTar(tarOut, ociLayoutFile, "oci-layout");

            // append specify manifest file -> blobs/sha256/abc123xxx
            File specifyManifestFile = buildSpecifyManifestFile(tempDir);
            TarUtil.appendFileToTar(tarOut, specifyManifestFile, "blobs/sha256/" + removeSharPrefix(manifest.getDigest()));

            // append config detail file -> blobs/sha256/123abc123xxx
            TarUtil.appendFileToTar(tarOut, configFile, "blobs/sha256/" + removeSharPrefix(specifyManifest.getConfig().getDigest()));

            // append all layer files
            for (File layoutFile : layerFileList) {
                TarUtil.appendFileToTar(tarOut, layoutFile, "blobs/sha256/" + removeSharPrefix(layoutFile.getName().replace(".tar", "")));
            }

            tarOut.finish();
            return tarFile;
        } catch (Exception e) {
            throw new ImageTarException("build image tar error");
        }
    }

    private File buildSpecifyManifestFile(TempDir tempDir) throws IOException {
        File specifyManifestFile = new File(tempDir.getPath().toFile(), "specifyManifestFile");
        Files.write(specifyManifestFile.toPath(), specifyManifest.toJsonString().getBytes());
        return specifyManifestFile;
    }


    private File buildIndexFile(TempDir tempDir) throws IOException {
        // index file is the manifests
        Manifests index = new Manifests();
        if (manifests.getManifestList() != null && !manifests.getManifestList().isEmpty()){
            index.setMediaType(manifests.getMediaType());
            index.setSchemaVersion(manifests.getSchemaVersion());
            index.setManifestList(Collections.singletonList(manifest));
        }
        File indexFile = new File(tempDir.getPath().toFile(), "index.json");
        Files.write(indexFile.toPath(), index.toJsonString().getBytes());
        return indexFile;
    }

    private File buildManifestFile(TempDir tempDir) throws IOException {
        TarManifest tarManifest = new TarManifest();
        String configDigest = "blobs/sha256/" + specifyManifest.getConfig().getDigest().split(Constant.COLON)[1];
        tarManifest.setConfig(configDigest);
        tarManifest.setRepoTags(Collections.singletonList(imageInfo.getOriginalImageName()));
        tarManifest.setLayers(specifyManifest.getLayers().stream().map(
            layer -> String.format("blobs/sha256/%s", layer.getDigest().split(Constant.COLON)[1])
        ).collect(Collectors.toList()));
        Map<String, Layer> layerSources = new HashMap<>(specifyManifest.getLayers().size());
        for (Layer layer : specifyManifest.getLayers()) {
            layerSources.put(layer.getDigest(), layer);
        }
        tarManifest.setLayerSources(layerSources);
        File manifestFile = new File(tempDir.getPath().toFile(), "manifest.json");
        Files.write(manifestFile.toPath(), tarManifest.toJsonString().getBytes());
        return manifestFile;
    }

    private File buildOciLayoutFile(TempDir tempDir) throws IOException {
        File ociLayoutFile = new File(tempDir.getPath().toFile(), "oci-layout");
        Files.write(ociLayoutFile.toPath(), DEFAULT_OCI_LAYOUT_CONTENT.getBytes());
        return ociLayoutFile;
    }


    private String removeSharPrefix(String digest) {
        return digest.split(Constant.COLON)[1];
    }
}
