package com.heybcat.docker.pull.core.pull;

import com.heybcat.docker.pull.core.async.AsyncBlob;
import com.heybcat.docker.pull.core.common.Constant;
import com.heybcat.docker.pull.core.common.ImageInfo;
import com.heybcat.docker.pull.core.common.PullContext;
import com.heybcat.docker.pull.core.common.oci.Layer;
import com.heybcat.docker.pull.core.common.oci.Manifest;
import com.heybcat.docker.pull.core.common.oci.Manifests;
import com.heybcat.docker.pull.core.common.oci.Platform;
import com.heybcat.docker.pull.core.exception.ImagePullException;
import com.heybcat.docker.pull.core.log.DefaultPullLogger;
import com.heybcat.docker.pull.core.log.IPullLogger;
import com.heybcat.docker.pull.core.registry.RegistryHelper;
import com.heybcat.docker.pull.core.registry.RegistryImageTarFactoryDefault;
import com.heybcat.docker.pull.util.SystemUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Fetters
 */
public class RegistryImagePuller {

    private final IPullLogger log;


    private RegistryImagePuller(IPullLogger log){
        this.log = Objects.requireNonNullElseGet(log, DefaultPullLogger::new);
    }


    private static final ThreadLocal<PullContext> PULL_CONTEXT = ThreadLocal.withInitial(PullContext::new);

    public static RegistryImagePuller create(IPullLogger log){
        return new RegistryImagePuller(log);
    }

    public File pull(String imageParameter, boolean optional){
        // step1: parse image parameter to ImageInfo
        log.msg("starting pull " + imageParameter);
        initImageInfo(imageParameter);

        // step2: get pull auth token
        authPullToken();
        log.msg("get pull token is done");

        // step3: get image manifest
        getImageManifest();

        // step4: determine which manifest to use
        specifyManifest(optional);

        //step5: download config file
        downloadConfigFile();

        // step6: download all layers
        downloadAllLayers();
        log.msg("all layers download finished");

        // step7: assembly image tar
        log.msg("starting assembly image tar");
        assemblyImageTar();
        log.msg("assembly image tar is done");

        File imageTarFile = PULL_CONTEXT.get().getImageTarFile();
        PULL_CONTEXT.remove();
        return imageTarFile;
    }

    private void initImageInfo(String imageParameter){
        ImageInfo imageInfo = parseImageParameter(imageParameter);
        PULL_CONTEXT.get().setImageInfo(imageInfo);
        log.msg("using proxy: " + imageInfo.getProxyAddress());
    }

    private static void authPullToken(){
        PULL_CONTEXT.get().setToken(RegistryHelper.getPullToken(PULL_CONTEXT.get().getImageInfo()));
    }

    private static void getImageManifest() {
        ImageInfo imageInfo = PULL_CONTEXT.get().getImageInfo();
        try {
            Manifests manifests = RegistryHelper.getManifests(imageInfo, null, PULL_CONTEXT.get().getToken());
            PULL_CONTEXT.get().setManifests(manifests);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new ImagePullException("Failed to get image manifest");
        }
    }

    private void specifyManifest(boolean optional){
        PullContext pullContext = PULL_CONTEXT.get();
        Manifests manifests = pullContext.getManifests();
        ImageInfo imageInfo = pullContext.getImageInfo();
        if (manifests.getManifestList() == null) {
            pullContext.setSpecifyManifest(manifests);
            return;
        }
        List<Manifest> manifestList = pullContext.getManifests().getManifestList();
        if (!optional) {
            // get cur platform manifest
            String os = SystemUtil.osName();
            String arch = SystemUtil.osArch();
            for (Manifest manifest : manifestList) {
                Platform platform = manifest.getPlatform();
                if (!platform.getOs().equals(os) || !platform.getArchitecture().equals(arch)) {
                    continue;
                }
                try {
                    pullContext.setChosenManifest(manifest);
                    Manifests specifyManifest = RegistryHelper.getManifests(imageInfo, manifest.getDigest(), pullContext.getToken(), manifest.getMediaType());
                    pullContext.setSpecifyManifest(specifyManifest);
                    imageInfo.setOs(manifest.getPlatform().getOs());
                    imageInfo.setArch(manifest.getPlatform().getArchitecture());
                    return;
                }catch (IOException | InterruptedException e){
                    Thread.currentThread().interrupt();
                    throw new ImagePullException("Failed to get image specify manifest");
                }
            }
            throw new ImagePullException("No matching platform manifest found");
        }else {
            // choose by user
            int choose = getConfigChoice(manifestList);
            Manifest manifest = manifestList.get(choose);
            pullContext.setChosenManifest(manifest);
            imageInfo.setOs(manifest.getPlatform().getOs());
            imageInfo.setArch(manifest.getPlatform().getArchitecture());
            try {
                Manifests specifyManifest = RegistryHelper.getManifests(imageInfo, manifest.getDigest(), pullContext.getToken(), manifest.getMediaType());
                pullContext.setSpecifyManifest(specifyManifest);
            }catch (IOException | InterruptedException e){
                Thread.currentThread().interrupt();
                throw new ImagePullException("Failed to get image specify manifest");
            }
        }

    }

    private int getConfigChoice(List<Manifest> manifestList) {
        // step1: print all manifest
        for (Manifest manifest : manifestList) {
            Platform platform = manifest.getPlatform();
            String logInfo = String.format("[%d] architecture: %s, os: %s, size: %d, digest: %s",
                manifestList.indexOf(manifest), platform.getArchitecture(), platform.getOs(),
                manifest.getSize(), manifest.getDigest());
            log.msg(logInfo);
        }
        // step2: wait user input which manifest to use
        log.msg("choose one to pull:");
        int choose = -1;
        Scanner scanner = new Scanner(System.in);
        while (choose == -1) {
            String line = scanner.nextLine();
            int input = Integer.parseInt(line);
            if (input >= manifestList.size() || input < 0) {
                log.warn("choose again");
            } else {
                choose = input;
            }
        }
        return choose;
    }

    private void downloadConfigFile() {
        PullContext pullContext = PULL_CONTEXT.get();
        Manifests config = pullContext.getSpecifyManifest();
        AsyncBlob asyncBlob = RegistryHelper.transferBlobs(pullContext.getImageInfo(), config.getConfig().getDigest(), pullContext.getToken(), Constant.CONFIG_SUFFIX);
        asyncBlob.waitForDone(null, null);
        pullContext.setConfigFile(asyncBlob.getBlobFile());
    }

    private void downloadAllLayers(){
        PullContext pullContext = PULL_CONTEXT.get();
        Manifests configManifests = pullContext.getSpecifyManifest();
        List<Layer> layers = configManifests.getLayers();
        List<AsyncBlob> asyncBlobList = new ArrayList<>();
        for (Layer layer : layers) {
            String digest = layer.getDigest();
            AsyncBlob asyncBlob = RegistryHelper.transferBlobs(pullContext.getImageInfo(), digest,
                pullContext.getToken(), Constant.TAR_SUFFIX);
            asyncBlobList.add(asyncBlob);
            asyncBlob.waitForDone(null, log);
        }
        pullContext.setLayerFileList(new ArrayList<>());
        asyncBlobList.forEach(blob -> pullContext.getLayerFileList().add(blob.getBlobFile()));
    }

    private static void assemblyImageTar(){
        PullContext pullContext = PULL_CONTEXT.get();
        try {
            File imageTarFile = RegistryImageTarFactoryDefault.builder()
                .manifest(pullContext.getChosenManifest())
                .layerFileList(pullContext.getLayerFileList())
                .configFile(pullContext.getConfigFile())
                .imageInfo(pullContext.getImageInfo())
                .manifests(pullContext.getManifests())
                .config(pullContext.getSpecifyManifest())
                .build();
            pullContext.setImageTarFile(imageTarFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ImagePullException("Failed to build image tar");
        }
    }

    /**
     * Parse docker image parameter into structured ImageInfo object
     * Supports following formats:
     * - Simple image: ubuntu
     * - Image with tag: ubuntu:latest
     * - Image with namespace: library/ubuntu
     * - Full image path: registry.example.com/custom/image:latest
     * - Image with digest: ubuntu@sha256:xxx
     * - Full path with digest: registry.example.com/custom/image@sha256:xxx
     * Default values:
     * - tag: latest (if not specified)
     * - namespace: library (if not specified)
     *
     * @param imageParameter Docker image parameter string
     * @return ImageInfo object containing parsed image information
     * @throws IllegalArgumentException if imageParameter is null or empty
     */
    public static ImageInfo parseImageParameter(String imageParameter) {
        if (StringUtils.isEmpty(imageParameter)) {
            throw new IllegalArgumentException("Image parameter cannot be null or empty");
        }

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setOriginalImageName(imageParameter);
        // Set default values
        imageInfo.setTag(Constant.DEFAULT_TAG);
        imageInfo.setNamespace(Constant.DEFAULT_NAMESPACE);

        // Handle digest format first
        String processedParameter = parseDigestIfPresent(imageParameter, imageInfo);

        // Parse the remaining path
        return parseImagePath(processedParameter, imageInfo);
    }

    private static String parseDigestIfPresent(String imageParameter, ImageInfo imageInfo) {
        if (imageParameter.contains(Constant.AT)) {
            String[] parts = imageParameter.split(Constant.AT, 2);
            imageInfo.setDigest(parts[1]);
            return parts[0];
        }
        return imageParameter;
    }

    private static ImageInfo parseImagePath(String parameter, ImageInfo imageInfo) {
        // Handle tag if present
        String[] tagParts = parameter.split(Constant.COLON, 2);
        if (tagParts.length == 2) {
            imageInfo.setTag(tagParts[1]);
        }

        String imagePath = tagParts[0];
        String[] pathParts = imagePath.split(Constant.SLASH);

        switch (pathParts.length) {
            case 1:
                // Simple image: ubuntu
                imageInfo.setImage(pathParts[0]);
                break;

            case 2:
                // namespace/image
                imageInfo.setNamespace(pathParts[0]);
                imageInfo.setImage(pathParts[1]);
                break;

            case 3:
                // registry/namespace/image
                imageInfo.setService(pathParts[0]);
                imageInfo.setNamespace(pathParts[1]);
                imageInfo.setImage(pathParts[2]);
                break;

            default:
                // Handle cases with more than 3 parts
                // registry/namespace/path/to/image
                imageInfo.setService(pathParts[0]);
                // The namespace should be the first part after the service
                imageInfo.setNamespace(pathParts[1]);
                // The image should be a combination of the remaining parts
                String image = String.join("/", Arrays.copyOfRange(pathParts, 2, pathParts.length));
                imageInfo.setImage(image);
                break;
        }

        return imageInfo;
    }





}
