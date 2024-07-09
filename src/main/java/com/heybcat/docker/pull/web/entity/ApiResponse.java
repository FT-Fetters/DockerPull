package com.heybcat.docker.pull.web.entity;

/**
 * @author Fetters
 */
public class ApiResponse<T> {

    private final Integer code;

    private final String msg;

    private final T data;

    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> success(){
        return new ApiResponse<>(0, "success", null);
    }

    public static <T> ApiResponse<T> fail(String msg){
        return new ApiResponse<>(-1, msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
