package com.tiny.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
//Jackson 是 Java 生态里处理 JSON 的绝对霸主（Spring Boot 默认用的就是它）。
@Slf4j
public class JsonSerializer implements CommonSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化错误: ", e);
            return null;
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("反序列化错误: ", e);
            return null;
        }
    }

    @Override
    public int getCode() {
        return 1; // 假设 1 代表 JSON
    }
}