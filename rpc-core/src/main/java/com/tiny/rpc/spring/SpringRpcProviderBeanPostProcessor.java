package com.tiny.rpc.spring;

import com.tiny.rpc.annotation.RpcService;
import com.tiny.rpc.registry.NacosServiceRegistry;
import com.tiny.rpc.registry.ServiceRegistry;
import com.tiny.rpc.server.NettyServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Slf4j
@Component
public class SpringRpcProviderBeanPostProcessor implements BeanPostProcessor, CommandLineRunner {

    @Autowired
    private ServiceRegistry serviceRegistry;

    // 从配置文件读取端口，默认 9999
    @Value("${rpc.server.port:9999}")
    private int port;
    @Value("${rpc.server.host:127.0.0.1}")
    private String host;
    /**
     * Bean 初始化后执行：扫描 @RpcService 注解
     */
    @Override
    @SneakyThrows
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 如果 Bean 上有 @RpcService 注解
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            // 1. 获取服务名 (接口名)
            String serviceName = bean.getClass().getInterfaces()[0].getCanonicalName();

            // 2. 注册到本地 (供 Netty 收到请求后查找)
            serviceRegistry.register(bean);

            // 3. 注册到 Nacos (供客户端发现)
            String host = InetAddress.getLocalHost().getHostAddress();
            NacosServiceRegistry.registerService(serviceName, new InetSocketAddress(host, port));

            log.info(">>> [服务发布] {} 注册至 Nacos {}:{}", serviceName, host, port);
        }
        return bean;
    }

    /**
     * Spring 容器启动完成后执行：启动 Netty Server
     */
    @Override
    public void run(String... args) {
        // 新开线程启动 Netty，防止阻塞 Spring 主线程
        new Thread(() -> {
            // 这里需要你的 NettyServer 构造函数接收 ServiceRegistry
            new NettyServer(serviceRegistry).start(host, port);
        }).start();
    }
}