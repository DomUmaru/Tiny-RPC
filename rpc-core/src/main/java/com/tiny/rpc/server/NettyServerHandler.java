package com.tiny.rpc.server;

import com.rpc.common.entity.RpcRequest;
import com.rpc.common.entity.RpcResponse;
import com.tiny.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 真正处理 RPC 请求的 Handler
 */
@Slf4j
//SimpleChannelInboundHandler：Netty的父类，简单通道（入站）
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceRegistry serviceRegistry;
    private final RequestHandler requestHandler;

    public NettyServerHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.requestHandler = new RequestHandler();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        log.info("服务端接收到请求: {}", msg);

        // 1. 从注册表中找实现类
        String interfaceName = msg.getInterfaceName();
        Object service = serviceRegistry.getService(interfaceName);

        RpcResponse<Object> response;
        if (service == null) {
            response = RpcResponse.fail("未找到服务: " + interfaceName);
            ctx.writeAndFlush(response);
        } else {
            // 2. 反射执行
            Object result = requestHandler.handle(msg, service);
            ctx.writeAndFlush(result);
        }
    }
}