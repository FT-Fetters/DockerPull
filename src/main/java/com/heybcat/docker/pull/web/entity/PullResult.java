package com.heybcat.docker.pull.web.entity;

/**
 * @author Fetters
 */
public class PullResult {

    public PullResult(boolean success, String msg){
        this.success = success;
        this.msg = msg;
    }

    private boolean success;

    private String msg;

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }
}
