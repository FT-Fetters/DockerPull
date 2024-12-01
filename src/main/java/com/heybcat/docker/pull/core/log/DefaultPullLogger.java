package com.heybcat.docker.pull.core.log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fetters
 */
public class DefaultPullLogger implements IPullLogger{

    private final Map<String, PullProgressBar> progressBarMap;

    public DefaultPullLogger(){
        this.progressBarMap = new HashMap<>();
    }

    @Override
    public void msg(String msg) {
        System.out.println(ConsoleStyle.green("➤") + " - " + msg);
    }

    @Override
    public void warn(String msg){
        System.out.println(ConsoleStyle.yellow("⚠") + " - " + msg);
    }

    @Override
    public void err(String msg) {
        System.out.println(ConsoleStyle.red("×") + " - " + msg);
    }

    @Override
    public void debug(String msg) {
        //                        🐞
        System.out.println("\uD83D\uDC1E" + "- " + msg);
    }

    @Override
    public void downloadProgress(String flag, long current, long total) {
        PullProgressBar pullProgressBar = progressBarMap.get(flag);
        if (pullProgressBar == null){
            pullProgressBar = PullProgressBar.create(total, "B", flag, true);
        }
        pullProgressBar.updateAndPrint(current);
        progressBarMap.put(flag, pullProgressBar);
    }
}
