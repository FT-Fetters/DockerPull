package com.heybcat.docker.pull.util;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

/**
 * @author Fetters
 */
public class SystemUtil {

    private SystemUtil() {}

    private static final String TEST_URL = "https://hub.docker.com/";

    public static InetSocketAddress getSystemProxy() {
        try {
            // 获取系统代理选择器
            ProxySelector proxySelector = ProxySelector.getDefault();
            if (proxySelector == null) {
                return null;
            }

            // 使用一个测试URL来获取代理设置
            URI uri = new URI(TEST_URL);
            List<Proxy> proxyList = proxySelector.select(uri);

            // 遍历代理列表
            for (Proxy proxy : proxyList) {
                // 跳过直连类型
                if (proxy.type() == Proxy.Type.DIRECT) {
                    continue;
                }

                // 获取代理地址
                SocketAddress address = proxy.address();
                if (address instanceof InetSocketAddress) {
                    InetSocketAddress socketAddress = (InetSocketAddress) address;
                    // 确保地址是有效的
                    if (!socketAddress.isUnresolved()) {
                        return socketAddress;
                    }
                }
            }

            // 如果没有找到代理设置，尝试从系统属性中获取
            String httpProxyHost = System.getProperty("http.proxyHost");
            String httpProxyPort = System.getProperty("http.proxyPort");

            if (httpProxyHost != null && !httpProxyHost.isEmpty() && httpProxyPort != null && !httpProxyPort.isEmpty()) {
                return new InetSocketAddress(httpProxyHost, Integer.parseInt(httpProxyPort));
            }

            // 如果都没有找到，返回null
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    public static String osName() {
        return System.getProperty("os.name");
    }

    public static String osArch() {
        return System.getProperty("os.arch");
    }

}
