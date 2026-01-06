package com.tiny.rpc.client;

import com.rpc.common.entity.RpcResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    // Key: requestId, Value: 未完成的 Future
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> FUTURE_MAP = new ConcurrentHashMap<>();

    public static void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        FUTURE_MAP.put(requestId, future);
    }

    public static void complete(RpcResponse<Object> rpcResponse) {
        // 根据响应中的 ID 找到对应的 Future
        CompletableFuture<RpcResponse<Object>> future = FUTURE_MAP.remove(rpcResponse.getRequestId());
        if (future != null) {
            // 这一步最关键：唤醒在那边傻等的 future.get()
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException("发现未知的响应 ID: " + rpcResponse.getRequestId());
        }
    }
}