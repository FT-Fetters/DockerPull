package com.heybcat.docker.pull.web.api;

import com.heybcat.docker.pull.util.CryptoUtil;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.docker.pull.web.entity.ApiResponse;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;

/**
 * @author Fetters
 */
@Cat
@WebEndpoint
@WebMapping("/api/config")
public class ConfigApi {

    @WebMapping("/set/proxy")
    public ApiResponse<String> setProxy(String proxyHost, String proxyPort){
        GlobalConfig.setProxyHost(proxyHost);
        GlobalConfig.setProxyPort(proxyPort);
        return ApiResponse.success();
    }

    @WebMapping("/set/ssh")
    public ApiResponse<String> setSsh(String sshHost, String sshPort, String sshUser, String sshPassword, String sshSavePath){
        GlobalConfig.setSshHost(sshHost);
        GlobalConfig.setSshPort(sshPort);
        GlobalConfig.setSshUser(sshUser);
        GlobalConfig.setSshPassword(sshPassword);
        GlobalConfig.setSshSavePath(sshSavePath);
        return ApiResponse.success();
    }

}
