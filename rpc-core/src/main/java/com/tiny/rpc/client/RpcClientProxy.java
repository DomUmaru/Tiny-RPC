package com.tiny.rpc.client;

import com.rpc.common.entity.RpcRequest;
import com.rpc.common.entity.RpcResponse;
import com.tiny.rpc.loadbalancer.LoadBalancer;
import com.tiny.rpc.loadbalancer.RandomLoadBalancer;
import com.tiny.rpc.registry.NacosServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * RPC 客户端代理
 * 作用：拦截方法调用，封装成 Request，发给 Netty
 */
@Slf4j
//InvocationHandler：调用处理器，“凡是这个替身对象收到的所有方法调用，都会被拦截，并转交给 invoke 方法来处理。”
public class RpcClientProxy implements InvocationHandler {

    public RpcClientProxy() {
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        // JDK 动态代理的三要素：类加载器、接口列表、InvocationHandler(this)
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),//类加载器：把对象加载到内存中
                new Class<?>[]{clazz},//接口列表：告诉 Java 这个替身要长得像谁，要实现哪些方法
                this//指定了这个替身的大脑是谁
        );
    }

    /**
     * 当你调用 helloService.sayHello() 时，实际执行的是这个 invoke 方法
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());

        // 1. 构建请求对象 Request
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .interfaceName(method.getDeclaringClass().getName()) // 拿到 "com.tiny.rpc.HelloService"
                .methodName(method.getName())                        // 拿到 "hello"
                .parameters(args)                                    // 拿到 ["程琪淋"]
                .paramTypes(method.getParameterTypes())              // 拿到 [String.class]
                .build();

        // 从 Nacos 获取服务地址 ---
        String serviceName = method.getDeclaringClass().getName();
        InetSocketAddress address = NacosServiceRegistry.lookupService(serviceName, new RandomLoadBalancer());
        if (address == null) {
            throw new RuntimeException("服务未发现: " + serviceName);
        }
        // --- 发送请求 ---
        NettyClient nettyClient = new NettyClient();
        RpcResponse response = nettyClient.sendRequest(rpcRequest, address.getHostName(), address.getPort());

        // 3. 处理结果
        if (response == null) {
            throw new RuntimeException("服务调用失败，响应为空");
        }
        if (response.getCode() == null || response.getCode() != 200) {
            throw new RuntimeException("服务报错: " + response.getMessage());
        }

        return response.getData();
    }
}