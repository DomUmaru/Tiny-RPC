package com.tiny.rpc.client;

import com.rpc.common.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse<Object>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse<Object> msg) {
        log.info("客户端收到响应: {}", msg);
        //唤醒Map中的Future
        UnprocessedRequests.complete(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }
}