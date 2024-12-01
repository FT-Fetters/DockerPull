package com.heybcat.docker.pull.core.log;

/**
 * @author Fetters
 */
public interface IPullLogger {

    void msg(String msg);

    void warn(String msg);

    void err(String msg);

    void debug(String msg);

    void downloadProgress(String flag, long current, long total);


}
