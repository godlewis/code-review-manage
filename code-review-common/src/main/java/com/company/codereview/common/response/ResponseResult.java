package com.company.codereview.common.response;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class ResponseResult<T> {
    
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public ResponseResult() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> ResponseResult<T> success() {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }
    
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = success();
        result.setData(data);
        return result;
    }
    
    public static <T> ResponseResult<T> success(String message, T data) {
        ResponseResult<T> result = success(data);
        result.setMessage(message);
        return result;
    }
    
    public static <T> ResponseResult<T> error(Integer code, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    public static <T> ResponseResult<T> error(String message) {
        return error(500, message);
    }
}