package com.heybcat.docker.pull.web.config;

import com.heybcat.docker.pull.util.CryptoUtil;

/**
 * @author Fetters
 */
public class GlobalConfig {

    private GlobalConfig(){

    }

    @Config("proxyHost")
    private static String proxyHost;

    @Config("proxyPort")
    private static String proxyPort;

    @Config("sshHost")
    private static String sshHost;

    @Config("sshPort")
    private static String sshPort;

    @Config("sshUser")
    private static String sshUser;

    @Config("sshPassword")
    private static String sshPassword;

    @Config("sshSavePath")
    private static String sshSavePath;

    @Config("isLoadImage")
    private static String isLoadImage;

    public static String getProxyHost() {
        return proxyHost;
    }

    public static void setProxyHost(String proxyHost) {
        GlobalConfig.proxyHost = proxyHost;
    }

    public static String getProxyPort() {
        return proxyPort;
    }

    public static void setProxyPort(String proxyPort) {
        GlobalConfig.proxyPort = proxyPort;
    }

    public static String getSshHost() {
        return sshHost;
    }

    public static void setSshHost(String sshHost) {
        GlobalConfig.sshHost = sshHost;
    }

    public static String getSshPort() {
        return sshPort;
    }

    public static void setSshPort(String sshPort) {
        GlobalConfig.sshPort = sshPort;
    }

    public static String getSshUser() {
        return sshUser;
    }

    public static void setSshUser(String sshUser) {
        GlobalConfig.sshUser = sshUser;
    }

    public static String getSshPassword() {
        if (sshPassword.matches("^[_+]+$")){
            return sshPassword;

        }else {
            return CryptoUtil.moduloEncrypt(sshPassword);
        }
    }

    public static void setSshPassword(String sshPassword) {
        if (sshPassword.matches("^[_+]+$")) {
            GlobalConfig.sshPassword = CryptoUtil.moduloDecrypt(sshPassword);
        }else {
            GlobalConfig.sshPassword = sshPassword;
        }
    }

    public static String getSshSavePath() {
        return sshSavePath;
    }

    public static void setSshSavePath(String sshSavePath) {
        GlobalConfig.sshSavePath = sshSavePath;
    }

    public static boolean getIsLoadImage() {
        return isLoadImage.equals("true");
    }

    public static void setIsLoadImage(String isLoadImage) {
        GlobalConfig.isLoadImage = isLoadImage;
    }
}
