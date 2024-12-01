package com.heybcat.docker.pull.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * @author Fetters
 */
public class TarUtil {

    private TarUtil() {}

    public static void appendFileToTar(TarArchiveOutputStream tarOut, File file, String entryName)
        throws IOException {
        TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
        tarOut.putArchiveEntry(tarEntry);
        Files.copy(file.toPath(), tarOut);
        tarOut.closeArchiveEntry();
    }

}
