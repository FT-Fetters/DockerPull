package com.heybcat.docker.pull;

import com.heybcat.docker.pull.core.DockerPull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * @author Fetters
 */
public class CommandStartup {

    static Logger log = Logger.getLogger(CommandStartup.class.getName());

    public static void main(String[] args)
        throws URISyntaxException, IOException, InterruptedException {
        if (args.length == 0){
            log.info("Usage: java -jar docker-pull.jar <image> <proxyUrl> <proxyPort>");
            return;
        }
        DockerPull.pull(args[0], args[1], Integer.parseInt(args[2]));
    }

}
