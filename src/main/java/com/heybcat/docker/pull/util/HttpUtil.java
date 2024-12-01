package com.heybcat.docker.pull.util;

import com.heybcat.docker.pull.core.ClientBuilder;
import com.heybcat.docker.pull.core.common.ImageInfo;
import java.net.http.HttpClient;

/**
 * @author Fetters
 */
public class HttpUtil {

    private HttpUtil(){}

    public static HttpClient buildHttpClient(ImageInfo imageInfo) {
        return ClientBuilder.build(imageInfo.getProxyAddress().getHostName(), imageInfo.getProxyAddress().getPort());
    }

}
