package com.heybcat.docker.pull.core.registry;

import com.heybcat.docker.pull.core.common.Constant;
import com.heybcat.docker.pull.core.common.ImageInfo;
import com.heybcat.docker.pull.core.common.TempDir;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
@Slf4j
public class RegistryImageTarFactoryBase extends AbstractRegistryImageTarFactory {

    private ImageInfo imageInfo;

    private List<File> layerFileList;

    private File configFile;

    private Manifests manifests;

    private Manifests config;

    private Manifest manifest;

    private RegistryImageTarFactoryBase() {}

    public static AbstractRegistryImageTarFactory builder(){
        return new RegistryImageTarFactoryBase();
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
        this.config = config;
        return this;
    }

    @Override
    public AbstractRegistryImageTarFactory manifest(Manifest manifest) {
        this.manifest = manifest;
        return this;
    }


    @Override
    public File build() throws IOException {
        // check all parameters first
        parameterVerify();

        File tarFile = outputTarFile();
        Files.createDirectories(tarFile.getParentFile().toPath());

        try (OutputStream outputStream = Files.newOutputStream(tarFile.toPath());
            BufferedOutputStream bufferedOut = new BufferedOutputStream(outputStream);
            GZIPOutputStream gzipOut = new GZIPOutputStream(bufferedOut);
            TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut);
            TempDir tempDir = new TempDir()) {

            // append config.json
            TarUtil.appendFileToTar(tarOut, configFile, removeSharPrefix(configFile.getName()));

            // append manifest.json
            File manifestFile = buildManifestFile(tempDir);
            TarUtil.appendFileToTar(tarOut, manifestFile, "manifest.json");

            // append repositories file
            File repositoriesFile = buildRepositoriesFile(tempDir);
            TarUtil.appendFileToTar(tarOut, repositoriesFile, "repositories");

            // append all layer files
            for (File layerFile : layerFileList) {
                if (layerFile == null){
                    continue;
                }
                log.info("add layer file: {}", layerFile.getName());

                // abc123.tar -> abc123/layer.tar
                TarUtil.appendFileToTar(tarOut, layerFile, removeSharPrefix(layerFile.getName()).replace(".tar", "/layer.tar"));
            }
            tarOut.finish();
            return tarFile;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImageTarException("build image tar error");
        }

    }

    private String removeSharPrefix(String digest) {
        return digest.split(Constant.COLON)[1];
    }


    private File buildRepositoriesFile(TempDir tempDir) throws IOException {
        String key = imageInfo.getFullImageName();
//        if (StringUtil.isNotBlank(imageInfo.getService())){
//            key = imageInfo.getService() + Constant.SLASH + key;
//        }
        String value = imageInfo.getTag();
        String content = String.format("{\"%s\":\"%s\"}", key, value);
        File repositoriesFile = new File(tempDir.getPath().toFile(), "repositories");
        Files.write(repositoriesFile.toPath(), content.getBytes());
        return repositoriesFile;
    }

    private File buildManifestFile(TempDir tempDir) throws IOException {
        TarManifest tarManifest = new TarManifest();
        String configDigest = config.getConfig().getDigest().split(Constant.COLON)[1];
        // like f356...511734.json
        tarManifest.setConfig(configDigest + Constant.DOT + Constant.CONFIG_SUFFIX);

        // build repoTags
        String repoTags = imageInfo.getFullImageName();
//        if (StringUtil.isNotBlank(imageInfo.getService())){
//            repoTags = imageInfo.getService() + Constant.SLASH + repoTags;
//        }
        if (StringUtil.isNotBlank(imageInfo.getTag())){
            repoTags = repoTags + Constant.COLON + imageInfo.getTag();
        }else {
            if (StringUtil.isNotBlank(imageInfo.getDigest())){
                repoTags = repoTags + Constant.AT + imageInfo.getDigest();
            }
        }
        tarManifest.setRepoTags(List.of(repoTags));

        // build layers
        tarManifest.setLayers(config.getLayers().stream().map(layer -> {
            String digest = layer.getDigest().split(Constant.COLON)[1];
            return String.format("%s/layer.tar", digest);
        }).collect(Collectors.toList()));

        // write manifest.json file
        File manifestFile = new File(tempDir.getPath().toFile(), "manifest.json");
        Files.write(manifestFile.toPath(), tarManifest.toJsonString().getBytes());
        return manifestFile;
    }

    private File outputTarFile() {
        return new File(String.format("%s/images/%s.%s",
            new File("").getAbsolutePath(), imageInfo.getFullImageName(), Constant.TAR_SUFFIX)
        );
    }

    private void parameterVerify() {
        if (imageInfo == null) {
            throw new IllegalArgumentException("imageInfo is null");
        }
        if (layerFileList == null) {
            throw new IllegalArgumentException("layerFileList is null");
        }
        if (configFile == null) {
            throw new IllegalArgumentException("configFile is null");
        }
        if (manifests == null) {
            throw new IllegalArgumentException("manifests is null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
    }


}
