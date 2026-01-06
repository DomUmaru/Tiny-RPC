package com.tiny.rpc.annotation;

import org.springframework.stereotype.Component;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者注解
 * 放在实现类上，表示该类发布 RPC 服务
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component // 让 Spring 能扫描到它
public @interface RpcService {
}