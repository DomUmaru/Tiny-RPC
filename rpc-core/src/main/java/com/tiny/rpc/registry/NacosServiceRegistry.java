package com.tiny.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.tiny.rpc.loadbalancer.LoadBalancer;
import com.tiny.rpc.loadbalancer.RandomLoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class NacosServiceRegistry {

    private static final String SERVER_ADDR = "127.0.0.1:8848"; // Docker Nacos 地址
    private static final NamingService namingService;

    static {
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("连接 Nacos 失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册服务
     * @param serviceName 接口全名 (com.rpc.api.HelloService)
     * @param address 服务端地址 (IP:Port)
     */
    public static void registerService(String serviceName, InetSocketAddress address) {
        try {
            namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
            log.info("向 Nacos 注册服务成功: {} -> {}", serviceName, address);
        } catch (NacosException e) {
            log.error("注册服务失败", e);
        }
    }

    /**
     * 服务发现 (获取一个可用的服务实例)
     * 这里简单实现：直接获取第一个 (后面可以做负载均衡)
     */
    public static InetSocketAddress lookupService(String serviceName, LoadBalancer loadBalancer) {
        try {
            List<Instance> instances = namingService.getAllInstances(serviceName);
            if (instances == null || instances.isEmpty()) {
                log.error("找不到服务: {}", serviceName);
                return null;
            }
            // 简单粗暴：取第一个 (Load Balance 伏笔)
            //Instance instance = instances.get(0);
            // --- 【修改】使用负载均衡器选择实例 ---
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            log.error("获取服务失败", e);
        }
        return null;
    }
}