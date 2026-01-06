package com.tiny.rpc.serializer;

//通用序列化器，策略模式
public interface CommonSerializer {
    // 序列化：对象 -> 字节数组
    byte[] serialize(Object obj);

    // 反序列化：字节数组 -> 对象
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    // 获得序列化器的编号 (用于协议包头，比如 1=JSON, 2=Kryo)
    int getCode();
}