package com.tiny.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import java.util.List;
import java.util.Random;

public class RandomLoadBalancer implements LoadBalancer {
    private final Random random = new Random();

    @Override
    public Instance select(List<Instance> instances) {
        if (instances == null || instances.isEmpty()) return null;
        // 经典的随机算法：nextInt(size)
        return instances.get(random.nextInt(instances.size()));
    }
}