package com.tiny.rpc.spring;

import com.tiny.rpc.annotation.RpcReference;
import com.tiny.rpc.client.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class SpringRpcConsumerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取类的所有字段 (包括 private)
        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field : fields) {
            // 如果字段上有 @RpcReference 注解
            if (field.isAnnotationPresent(RpcReference.class)) {
                try {
                    // 1. 生成代理对象
                    Class<?> serviceClass = field.getType();
                    RpcClientProxy proxy = new RpcClientProxy();
                    Object proxyObject = proxy.getProxy(serviceClass);

                    // 2. 暴力反射注入
                    field.setAccessible(true);
                    field.set(bean, proxyObject);

                    log.info(">>> [远程注入] 为 Bean '{}' 注入接口 {}", beanName, serviceClass.getName());
                } catch (IllegalAccessException e) {
                    log.error("注入 @RpcReference 失败", e);
                }
            }
        }
        return bean;
    }
}