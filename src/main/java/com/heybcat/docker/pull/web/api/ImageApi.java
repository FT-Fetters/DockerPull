package com.heybcat.docker.pull.web.api;

import com.heybcat.docker.pull.session.SessionManager;
import com.heybcat.docker.pull.session.SessionManager.PullSession;
import com.heybcat.docker.pull.web.entity.ApiResponse;
import com.heybcat.docker.pull.web.entity.view.LocalImagesView;
import com.heybcat.docker.pull.web.entity.view.UploadImageView;
import com.heybcat.docker.pull.web.service.DockerImageService;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.common.ioc.annotation.Inject;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
@Cat
@WebEndpoint
@WebMapping("/api/image")
public class ImageApi {

    private final DockerImageService dockerImageService;

    @Inject
    public ImageApi(DockerImageService dockerImageService) {
        this.dockerImageService = dockerImageService;
    }


    @WebMapping("/pull")
    public ApiResponse<String> pull(String namespace, String image, String tag, String os, String arch) {
        if (StringUtil.isAnyBlank(image, tag, os, arch)){
            return ApiResponse.fail("miss parameter");
        }
        if (StringUtil.isBlank(namespace)){
            namespace = "library";
        }
        String session = dockerImageService.pull(namespace, image, tag, os, arch);
        if (StringUtil.isBlank(session)){
            return ApiResponse.fail("pull failed");
        }
        return ApiResponse.success(session);
    }

    @WebMapping("/session/status")
    public ApiResponse<PullSession> pullStatus(String session) {
        PullSession pullSession = SessionManager.getInstance().getSession(session);
        if (pullSession == null){
            return ApiResponse.fail("session not found");
        }
        return ApiResponse.success(pullSession);
    }

    /**
     * get local images
     * @param cur cur page num
     * @param size cur page size
     * @param keyword file keyword
     * @param order ASC or DESC
     * @param orderBy time or size
     */
    @WebMapping("/local/images")
    public ApiResponse<LocalImagesView> localImages(Integer cur, Integer size, String keyword, String order, String orderBy){
        cur = cur == null ? 1 : cur;
        size = size == null ? 10 : size;
        return ApiResponse.success(dockerImageService.localImages(cur, size, keyword, order, orderBy));
    }

    /**
     * delete local image
     * @param fileName image file name
     */
    @WebMapping("/local/images/delete")
    public ApiResponse<String> deleteImage(String fileName){
        return ApiResponse.success(dockerImageService.deleteImage(fileName));
    }


    @WebMapping("/local/images/upload")
    public ApiResponse<UploadImageView> uploadImage(String fileName){
        return ApiResponse.success(dockerImageService.uploadImage(fileName));
    }
}
