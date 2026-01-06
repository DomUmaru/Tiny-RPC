package com.test.server;
import com.rpc.api.HelloService;
import com.tiny.rpc.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 真正的业务逻辑
@RpcService
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("接收到消息：{}", name);
        return "Hello, " + name + "! 这里是 Tiny-RPC 服务端";
    }
}