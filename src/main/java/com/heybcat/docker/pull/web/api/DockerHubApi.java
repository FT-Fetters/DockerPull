package com.heybcat.docker.pull.web.api;

import com.alibaba.fastjson2.JSONObject;
import com.heybcat.docker.pull.web.entity.ApiResponse;
import com.heybcat.docker.pull.web.entity.view.HubSearchView;
import com.heybcat.docker.pull.web.service.DockerHubService;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.common.ioc.annotation.Inject;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import java.io.IOException;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
@Cat
@WebEndpoint
@WebMapping("/api/docker/hub")
public class DockerHubApi {

    private final DockerHubService dockerHubService;

    @Inject
    public DockerHubApi(DockerHubService dockerHubService) {
        this.dockerHubService = dockerHubService;
    }

    @WebMapping("/search")
    public ApiResponse<HubSearchView> search(String image, Integer from, Integer size)
        throws IOException, InterruptedException {
        if (StringUtil.isBlank(image)){
            return ApiResponse.fail("miss image name");
        }
        from = from == null ? 0 : from;
        size = size == null ? 10 : size;
        HubSearchView view = dockerHubService.search(image, from, size);
        if (view != null){
            return ApiResponse.success(view);
        }else {
            return ApiResponse.fail("search fail");
        }
    }

    @WebMapping("/tags")
    public ApiResponse<JSONObject> tags(String id, Integer from, Integer size)
        throws IOException, InterruptedException {
        if (StringUtil.isBlank(id)){
            return ApiResponse.fail("miss image id");
        }
        from = from == null ? 0 : from;
        size = size == null ? 10 : size;
        JSONObject tags = dockerHubService.tags(id, from, size);
        if (tags != null){
            return ApiResponse.success(tags);
        }else {
            return ApiResponse.fail("get tags fail");
        }
    }



}
