package com.heybcat.docker.pull.web;

import com.heybcat.tightlyweb.TightlyWebApplication;
import com.heybcat.tightlyweb.annoation.TightlyWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fetters
 */
@TightlyWeb(basePackage = "com.heybcat.docker.pull.web")
public class DockerPullWebApplication {

    private static final Logger log = LoggerFactory.getLogger(DockerPullWebApplication.class);

    private DockerPullWebApplication(){
        throw new IllegalStateException();
    }

    public static void run(){
        log.info("load web...");
        TightlyWebApplication.run(DockerPullWebApplication.class);
    }

}
