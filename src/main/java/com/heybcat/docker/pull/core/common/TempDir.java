package com.heybcat.docker.pull.core.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;

/**
 * @author Fetters
 */
public class TempDir implements AutoCloseable {

    private final Path tempPath;

    public TempDir() throws IOException {
        this.tempPath = Files.createTempDirectory("docker-pull-");
    }

    public Path getPath() {
        return tempPath;
    }

    @Override
    public void close() throws Exception {
        // del temp dir
        FileUtils.deleteDirectory(tempPath.toFile());
    }
}
