package com.heybcat.docker.pull.web.service;

import com.heybcat.docker.pull.core.pull.BaseDockerPull;
import com.heybcat.docker.pull.session.PullSessionManager;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Fetters
 */
@Cat
public class DockerImageService {

    private final ThreadPoolExecutor threadPoolExecutor;

    public DockerImageService() {
        this.threadPoolExecutor = new ThreadPoolExecutor(
            8, 8, 0L, java.util.concurrent.TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), r -> new Thread(r, "docker-pull-" + r.hashCode()));
    }

    public String pull(String namespace, String image, String tag, String os, String arch) {
        String session = PullSessionManager.getInstance().newSession();
        threadPoolExecutor.execute(
            () -> BaseDockerPull.pull(null, namespace, image, tag, os, arch,
                GlobalConfig.getProxyHost(),
                GlobalConfig.getProxyPort() != null ? Integer.valueOf(GlobalConfig.getProxyPort())
                    : null, session)
        );
        return session;
    }

}
