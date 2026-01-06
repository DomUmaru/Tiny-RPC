package com.test.server;

import com.rpc.api.HelloService;
import com.tiny.rpc.registry.ServiceRegistry;
import com.tiny.rpc.server.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
1、客户端启动：生成接口的动态代理（服务员）。
2、拦截调用：用户调用方法，代理在 invoke 中把所有信息封装成 RpcRequest 对象（订单）。
3、网络传输：Netty 把 RpcRequest序列化成二进制，通过网线发给服务端。
4、服务端接收：Netty 收到二进制，反序列化还原出 RpcRequest。
5、查表：服务端根据 RpcRequest 里的接口名，在 Map（注册表）里找到提前注册好的 Service 实例。
6、反射执行：利用 RpcRequest 里的方法名和参数，通过 反射 (method.invoke)，让 Service 实例执行真正的逻辑。
7、返回：把结果封装成 RpcResponse 发回去。
 */

// 扫描 rpc-core 里的 BeanPostProcessor
@SpringBootApplication(scanBasePackages = {"com.test.server", "com.tiny.rpc"})
public class SocketServerMain {
    public static void main(String[] args) {
//        // 1. 初始化注册中心
//        ServiceRegistry serviceRegistry = new ServiceRegistry();
//
//        // 2. 注册服务 (把接口名和实现类关联起来)
//        HelloService helloService = new HelloServiceImpl();
//        serviceRegistry.register(helloService);
//
//        // 3. 启动 Netty 服务器，监听 9999 端口
//        NettyServer server = new NettyServer(serviceRegistry);
//        server.start("127.0.0.1",9999);
        //通过Spring注解实现IOC
        SpringApplication.run(SocketServerMain.class, args);
    }
}