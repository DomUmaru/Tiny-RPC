package com.tiny.rpc.server;

import com.tiny.rpc.codec.CommonDecoder;
import com.tiny.rpc.codec.CommonEncoder;
import com.tiny.rpc.registry.NacosServiceRegistry;
import com.tiny.rpc.registry.ServiceRegistry;
import com.tiny.rpc.serializer.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyServer {

    private final ServiceRegistry serviceRegistry; // 服务注册表

    public NettyServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void start(String host, int port) {
        // 1. Boss 线程组：负责揽客（连接）
        //NioEventLoopGroup: 死循环线程池，接受客户端TCP连接，生成Socket扔给WorkerGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 2. Worker 线程组：负责干活（读写数据），默认 CPU核数 * 2
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //ServerBootstrap：辅助启动配置类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)// 前接受，后执行
                    .channel(NioServerSocketChannel.class)// ServerSocketChannel封装，指定通道类型
                    .option(ChannelOption.SO_BACKLOG, 256)// 设置TCP参数，全连接队列大小
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)// 配置Worker线程，降低延迟
                    //初始化SocketChannel
                    .childHandler(new ChannelInitializer<SocketChannel>() {// 配置流水线（Pipeline）
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 编解码器
                            // 解码器：入站（Byte -> Object）
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                            // 编码器：出站（Object -> Byte）
                            pipeline.addLast(new CommonDecoder());
                            // 业务逻辑
                            pipeline.addLast(new NettyServerHandler(serviceRegistry));
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(host, port).sync();

            // 注册服务到 Nacos
            NacosServiceRegistry.registerService("com.rpc.api.HelloService", new InetSocketAddress(host, port));

            log.info("RPC 服务端启动，监听端口: {}", port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动服务器时发生异常:", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}