package com.test.client;

import com.rpc.api.HelloService;
import com.tiny.rpc.client.RpcClientProxy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.test.client", "com.tiny.rpc"})
public class SocketClientMain {
    public static void main(String[] args) {
//        // 1. 创建代理工厂 (告诉它服务端在哪：127.0.0.1:9999)
//        RpcClientProxy proxy = new RpcClientProxy();
//
//        // 2. 获取 HelloService 的代理对象 (魔法发生的地方！)
//        HelloService helloService = proxy.getProxy(HelloService.class);
//
//        // 3. 像调用本地方法一样调用远程方法
//        System.out.println("开始调用...");
//        String result = helloService.sayHello("ACMer");
//
//        System.out.println("调用结果: " + result);
        SpringApplication.run(SocketClientMain.class, args);
    }
}