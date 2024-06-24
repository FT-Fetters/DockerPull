package com.heybcat.docker.pull.web.api;

import com.heybcat.docker.pull.session.PullSessionManager;
import com.heybcat.docker.pull.session.PullSessionManager.PullSession;
import com.heybcat.docker.pull.web.entity.ApiResponse;
import com.heybcat.docker.pull.web.service.DockerImageService;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
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

    @WebMapping("/pull/status")
    public ApiResponse<PullSession> pullStatus(String session) {
        PullSession pullSession = PullSessionManager.getInstance().getSession(session);
        if (pullSession == null){
            return ApiResponse.fail("session not found");
        }
        return ApiResponse.success(pullSession);
    }
}
