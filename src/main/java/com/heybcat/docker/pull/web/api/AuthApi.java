package com.heybcat.docker.pull.web.api;

import com.heybcat.docker.pull.web.annoation.WhiteApi;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.docker.pull.web.entity.ApiResponse;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
@Cat
@WebEndpoint
@WebMapping("/api/auth")
public class AuthApi {

    @WebMapping("/isAuth")
    @WhiteApi
    public ApiResponse<Boolean> isAuth() {
        return ApiResponse.success(StringUtil.isNotBlank(GlobalConfig.getAuthKey()));
    }

    @WebMapping("/checkAuth")
    @WhiteApi
    public ApiResponse<Boolean> checkAuth(String authKey) {
        return ApiResponse.success(GlobalConfig.getAuthKey().equals(authKey));
    }

}
