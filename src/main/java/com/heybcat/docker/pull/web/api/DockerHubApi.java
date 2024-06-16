package com.heybcat.docker.pull.web.api;

import com.heybcat.docker.pull.web.entity.ApiResponse;
import com.heybcat.docker.pull.web.entity.view.HubSearchView;
import com.heybcat.docker.pull.web.service.DockerHubService;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.common.ioc.annotation.Inject;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import java.util.List;
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
    public ApiResponse<List<HubSearchView>> search(String image){
        if (StringUtil.isBlank(image)){
            return ApiResponse.fail("image is null");
        }
        return ApiResponse.success(dockerHubService.search(image));
    }



}
