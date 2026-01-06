package com.tiny.rpc.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册表
 * 保存 接口名 -> 实现类对象 的映射
 */
//因为用户端和客户端的实现类是不互通的，但是接口是互通的，所以用接口作为Key来查找实现类实例
@Component
public class ServiceRegistry {
    // key: 接口全名 (com.rpc.api.HelloService)
    // value: 实现类实例 (new HelloServiceImpl())
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public <T> void register(T service) {
        //getClass：得到service的实例，class com.rpc.server.HelloServiceImpl
        //getInterfaces：得到实现哪些接口，返回Class数组[ interface com.rpc.api.HelloService]
        //[0]：数组第一个元素
        //getCanonicalName：读取这个Class对象的名字属性，返回字符串"com.rpc.api.HelloService"
        String serviceName = service.getClass().getInterfaces()[0].getCanonicalName();
        serviceMap.put(serviceName, service);
        System.out.println(">>> [本地注册] 服务: " + serviceName);
    }

    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }
}