package com.heybcat.docker.pull.core.common;

import com.heybcat.docker.pull.util.SystemUtil;
import java.net.InetSocketAddress;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Fetters
 */
@Setter
@ToString
public class ImageInfo {

    @Getter
    private String service;

    @Getter
    private String namespace;

    @Getter
    private String image;

    @Getter
    private String tag;

    @Getter
    private String os;

    @Getter
    private String arch;

    @Getter
    private String digest;

    @Getter
    private String originalImageName;

    /**
     * 代理地址(地址+端口)
     */
    private InetSocketAddress proxyAddress;

    public static ImageInfo parse(String image) {
        ImageInfo imageInfo = new ImageInfo();
        if(image.contains(Constant.AT)){
            String[] imageSplit = image.split(Constant.AT);
            image = imageSplit[0];
            imageInfo.digest = imageSplit[1];
        } else if (image.contains(Constant.COLON)) {
            String[] imageSplit = image.split(Constant.COLON);
            image = imageSplit[0];
            imageInfo.tag = imageSplit[1];
        }
        String[] imageSplit = image.split("/");
        if (imageSplit.length == 3) {
            imageInfo.service = imageSplit[0];
            imageInfo.namespace = imageSplit[1];
            imageInfo.image = imageSplit[2];
        } else if (imageSplit.length == 2) {
            imageInfo.namespace = imageSplit[0];
            imageInfo.image = imageSplit[1];
        }else {
            imageInfo.image = imageSplit[0];
        }
        return imageInfo;
    }

    public InetSocketAddress getProxyAddress() {
        if(this.proxyAddress == null){
            this.proxyAddress = SystemUtil.getSystemProxy();
        }
        return proxyAddress;
    }

    public String getFullImageName(){
        return this.namespace + "/" + this.image;
    }
}
