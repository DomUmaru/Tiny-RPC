package com.tiny.rpc.serializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {

    private static final Map<Integer, CommonSerializer> serializerMap = new HashMap<>();

    static {
        // 预加载所有序列化器
        serializerMap.put(1, new JsonSerializer());
        serializerMap.put(2, new KryoSerializer());
    }

    public static CommonSerializer getByCode(int code) {
        CommonSerializer serializer = serializerMap.get(code);
        if (serializer == null) {
            // 默认为 JSON，或者抛异常
            return serializerMap.get(0);
        }
        return serializer;
    }
}