package com.tiny.rpc.client;
import com.rpc.common.entity.RpcRequest;
import com.rpc.common.entity.RpcResponse;
import com.tiny.rpc.codec.CommonDecoder;
import com.tiny.rpc.codec.CommonEncoder;
import com.tiny.rpc.serializer.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class NettyClient {

    private static final Bootstrap bootstrap;
    private static final EventLoopGroup group;

    
    // 初始化 Bootstrap (做成静态的，复用线程组)
    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                        pipeline.addLast(new CommonDecoder());
                        pipeline.addLast(new NettyClientHandler());
                    }
                });
    }


    public RpcResponse sendRequest(RpcRequest rpcRequest, String host, int port) {
        try {
            // 1. 创建一个“空头支票” (Future)
            CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();

            // 2. 把支票存根放进 Map，Key 是 RequestId
            UnprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);

            // 3. 连接并发送 (这里为了简单还是每次连接，优化点：可以用 ChannelPool 复用连接)
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();

            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    log.info("客户端发送消息成功: {}", rpcRequest.getRequestId());
                } else {
                    f.channel().close();
                    resultFuture.completeExceptionally(f.cause());
                    log.error("客户端发送消息失败", f.cause());
                }
            });

            // 4. 【异步转同步】阻塞等待结果
            // 这一行代码会卡住，直到 NettyClientHandler 里调用了 complete()
            return resultFuture.get();

        } catch (Exception e) {
            log.error("发送消息时有错误发生: ", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}