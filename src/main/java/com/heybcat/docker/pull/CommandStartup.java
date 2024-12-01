package com.heybcat.docker.pull;

import com.heybcat.docker.pull.core.ConfigManager;
import com.heybcat.docker.pull.core.log.DefaultPullLogger;
import com.heybcat.docker.pull.core.log.IPullLogger;
import com.heybcat.docker.pull.core.pull.CommandDockerPull;
import com.heybcat.docker.pull.core.pull.RegistryImagePuller;
import com.heybcat.docker.pull.web.DockerPullWebApplication;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * @author Fetters
 */
public class CommandStartup {

    private static final String WEB_FLAG = "--web";

    private static final IPullLogger log = new DefaultPullLogger();

    public static void main(String[] args)
        throws URISyntaxException, IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ConfigManager.load();
        handleCommand(args);
    }

    private static void handleCommand(String[] args)
        throws URISyntaxException, IOException, InterruptedException {
        if (args.length == 0) {
            logUsage();
            return;
        }

        if (args.length == 3) {
            CommandDockerPull.pull(args[0], args[1], Integer.parseInt(args[2]));
            return;
        }

        if (args.length == 1 && !args[0].equals("pull")) {
            if (WEB_FLAG.equals(args[0])) {
                DockerPullWebApplication.run();
            } else {
                CommandDockerPull.pull(args[0], null, null);
            }
            return;
        }

        if (args.length == 2 && args[0].equals("pull")){
            RegistryImagePuller puller = RegistryImagePuller.create(log);
            File imageFile = puller.pull(args[1], true);
            log.msg("image path: " + imageFile.getAbsolutePath());
            System.exit(0);
            return;
        }
        logUsage();
    }

    private static void logUsage() {
        log.msg("Usage: java -jar docker-pull.jar <image> <proxyUrl> <proxyPort>");
        log.msg("       java -jar docker-pull.jar --web");
        log.msg("       java -jar docker-pull.jar pull [SERVICE/]NAME[:TAG|@DIGEST]");
    }

}
