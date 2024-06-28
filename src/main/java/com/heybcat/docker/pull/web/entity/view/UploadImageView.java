package com.heybcat.docker.pull.web.entity.view;

/**
 * @author Fetters
 */
public class UploadImageView {

    private String session;

    private Boolean success;

    private String message;

    public UploadImageView(boolean b, String message) {
        this.success = b;
        this.message = message;
    }

    public static UploadImageView fail(String message){
        return new UploadImageView(false, message);
    }

    public UploadImageView(String session, Boolean success, String message) {
        this.session = session;
        this.success = success;
        this.message = message;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
