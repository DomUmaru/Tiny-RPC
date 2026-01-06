package com.rpc.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    private String requestId; // 新增：请求ID
    private String interfaceName; // 接口名
    private String methodName;    // 方法名
    private Object[] parameters;  // 参数
    private Class<?>[] paramTypes;// 参数类型
}
