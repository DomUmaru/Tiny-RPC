package com.rpc.common.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class RpcResponse<T> implements Serializable {
    private String requestId; // 新增：响应ID (必须与请求ID一致)
    private Integer code;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(200);
        response.setData(data);
        return response;
    }
    public static <T> RpcResponse<T> fail(String message) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }
}