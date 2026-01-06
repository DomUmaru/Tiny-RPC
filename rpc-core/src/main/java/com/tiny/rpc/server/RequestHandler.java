package com.tiny.rpc.server;

import com.rpc.common.entity.RpcRequest;
import com.rpc.common.entity.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class RequestHandler {

    public RpcResponse handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            // 1. 拿到“说明书” (Class对象)
            Class<?> clazz = service.getClass();
            // 2. 获取要调用的方法对象
            Method method = clazz.getMethod(
                    rpcRequest.getMethodName(),
                    rpcRequest.getParamTypes()
            );
            // 3. 反射调用 method.invoke(对象, 参数)
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("服务:{} 方法:{} 执行成功", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (Exception e) {
            log.error("发生错误", e);
            return RpcResponse.fail("调用失败" + e.getMessage());
        }
        // 封装成功响应
        RpcResponse<Object> response = RpcResponse.success(result);
        // 【关键】把请求的 ID 塞回去
        response.setRequestId(rpcRequest.getRequestId());
        return response;
    }
}
