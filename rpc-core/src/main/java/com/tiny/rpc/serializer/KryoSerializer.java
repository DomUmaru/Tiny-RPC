package com.tiny.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.rpc.common.entity.RpcRequest;
import com.rpc.common.entity.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class KryoSerializer implements CommonSerializer {

    // 重点：使用 ThreadLocal 解决 Kryo 非线程安全问题
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 支持循环引用（默认 true，设为 false 可以快一点，但有风险）
        kryo.setReferences(true);
        // 关闭注册行为（设为 true 时需要把所有类都注册，麻烦但性能高；false 方便开发）
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {

            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);

            // 必须 flush 否则数据可能写不全
            // ThreadLocal 不需要 remove，因为 Kryo 实例就是要复用的
            return output.toBytes();
        } catch (Exception e) {
            log.error("Kryo 序列化失败", e);
            throw new RuntimeException("Kryo 序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {

            Kryo kryo = kryoThreadLocal.get();
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            log.error("Kryo 反序列化失败", e);
            throw new RuntimeException("Kryo 反序列化失败");
        }
    }

    @Override
    public int getCode() {
        return 2; // 假设 1 是 JSON，2 是 Kryo
    }
}