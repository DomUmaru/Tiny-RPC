# Tiny-RPC - Lightweight RPC Framework

## 1. 项目概述 (Overview)

**Tiny-RPC** 是一个基于 Java 实现的轻量级、高性能 RPC (Remote Procedure Call) 框架。它旨在帮助开发者理解 RPC 的核心原理，实现了从服务注册发现、网络通信、序列化到动态代理的全链路 RPC 功能。

该项目结构清晰，模块解耦，非常适合作为学习 RPC 底层原理、Netty 网络编程以及 Java 动态代理的参考案例。

### 技术特点
- **高性能通信**: 基于 **Netty** 实现 NIO 异步非阻塞网络传输。
- **自定义协议**: 设计了包含 Magic Number、版本号、消息类型、序列化方式等信息的私有通信协议，解决 TCP 粘包/拆包问题。
- **多序列化支持**: 采用 SPI 机制（策略模式），默认支持 **Kryo** 高性能序列化和 **JSON** 序列化，可轻松扩展。
- **服务注册与发现**: 集成 **Nacos** 作为注册中心，支持服务的自动注册与发现，实现服务解耦。
- **负载均衡**: 客户端内置负载均衡策略（目前实现随机算法），支持集群调用。
- **Spring 集成**: 提供 `@RpcService` 和 `@RpcReference` 注解，利用 Spring BeanPostProcessor 实现无缝接入，开箱即用。

---

## 2. 核心架构设计 (Architecture)

### 架构概览

Tiny-RPC 采用经典的 Client-Server 架构，结合 Nacos 注册中心实现服务的动态治理。

```mermaid
graph TD
    subgraph Service Provider
        Server[Netty Server] --> |Register| Registry[Nacos Registry]
        Service[Service Implementation] -.-> |@RpcService| Server
    end

    subgraph Service Consumer
        Client[Netty Client] --> |Discover| Registry
        Proxy[RPC Proxy] --> |Invoke| Client
        App[Application] --> |@RpcReference| Proxy
    end

    Client <--> |Custom Protocol / TCP| Server
```

### 交互流程

1.  **服务启动**: 服务端启动 Netty Server，扫描 `@RpcService` 注解的 Bean，将其 IP 和端口注册到 Nacos，并缓存到本地服务表。
2.  **服务调用**: 客户端通过 `@RpcReference` 注入接口代理。调用接口方法时，代理对象拦截调用。
3.  **服务发现**: 客户端向 Nacos 查询服务实例列表，并使用负载均衡策略选择一个目标节点。
4.  **请求发送**: 客户端将调用信息（接口、方法、参数）封装为 `RpcRequest`，序列化后通过 Netty 发送。
5.  **异步等待**: 客户端使用 `CompletableFuture` 挂起当前线程，等待响应。
6.  **服务执行**: 服务端收到请求，解码后通过反射调用本地服务实现，将结果封装为 `RpcResponse` 返回。
7.  **结果返回**: 客户端收到响应，唤醒挂起的线程，返回执行结果。

---

## 3. 模块说明 (Modules)

| 模块名称 | 说明 |
| :--- | :--- |
| **rpc-api** | 定义通用的 RPC 接口（如 `HelloService`），供服务端和客户端共同依赖。 |
| **rpc-common** | 存放公共实体类（`RpcRequest`, `RpcResponse`）、枚举和工具类。 |
| **rpc-core** | **核心模块**。包含网络传输、协议编解码、序列化、注册中心、负载均衡、代理等核心实现。 |
| **test-server** | 服务端测试模块。演示如何启动 RPC 服务并暴露接口。 |
| **test-client** | 客户端测试模块。演示如何通过 Spring Boot 和注解调用远程服务。 |

---

## 4. 自定义协议 (Protocol)

为了高效传输并解决网络底层问题，Tiny-RPC 设计了如下的二进制协议格式：

```text
+---------------+---------------+-----------------+-------------+
| Magic Number  | Package Type  | Serializer Type | Data Length |
|   4 Bytes     |    4 Bytes    |     4 Bytes     |   4 Bytes   |
+---------------+---------------+-----------------+-------------+
|                          Data Body                            |
|                   (RpcRequest / RpcResponse)                  |
+---------------------------------------------------------------+
```

*   **Magic Number**: `0xCAFEBABE`，用于协议校验。
*   **Package Type**: 标识是请求包 (`REQUEST_PACK`) 还是响应包 (`RESPONSE_PACK`)。
*   **Serializer Type**: 标识使用的序列化器（如 Kryo, JSON），接收端据此选择反序列化方式。
*   **Data Length**: 数据体的长度，用于 Netty 的 `LengthFieldBasedFrameDecoder` 解决拆包问题。
*   **Data Body**: 序列化后的实际业务数据。

---

## 5. 快速开始 (Quick Start)

### 环境要求
*   JDK 1.8+
*   Maven 3.x
*   Nacos Server (需在本地 8848 端口启动)

### 步骤 1: 启动 Nacos
确保本地 Nacos 服务已启动（默认端口 8848）。

### 步骤 2: 编译项目
在项目根目录下执行：
```bash
mvn clean install
```

### 步骤 3: 启动服务端
运行 `test-server` 模块下的 `SocketServerMain2.java`。
> 注意：该示例手动启动了 NettyServer 并监听 9998 端口。

### 步骤 4: 启动客户端
运行 `test-client` 模块下的 `SocketClientMain.java`。
> 这是一个 Spring Boot 应用，启动后会暴露 Web 端口（默认 8080）。

### 步骤 5: 验证调用
在浏览器访问客户端提供的测试接口：
```
http://localhost:8080/hello
```
若成功，浏览器将显示服务端返回的：`Hello, Spring Consumer! 这里是 Tiny-RPC 服务端`。

---

## 6. API 使用示例 (Usage)

### 1. 定义服务接口
在 `rpc-api` 中定义接口：
```java
public interface HelloService {
    String sayHello(String name);
}
```

### 2. 服务端实现并暴露服务
使用 `@RpcService` 注解标记实现类：
```java
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
```

### 3. 客户端调用服务
在 Spring Bean 中使用 `@RpcReference` 注入代理：
```java
@RestController
public class TestController {

    @RpcReference
    private HelloService helloService;

    @GetMapping("/hello")
    public String hello() {
        return helloService.sayHello("User");
    }
}
```

---

## 7. 开发计划 (Roadmap)

- [x] 基于 Netty 的基础通信
- [x] Kryo / JSON 序列化支持
- [x] Nacos 服务注册与发现
- [x] 基础负载均衡 (Random)
- [x] Spring 注解集成
- [ ] 支持更多负载均衡策略 (RoundRobin, ConsistentHash)
- [ ] 增加心跳检测与断线重连机制
- [ ] 支持配置中心
- [ ] 完善的异常处理与熔断降级
