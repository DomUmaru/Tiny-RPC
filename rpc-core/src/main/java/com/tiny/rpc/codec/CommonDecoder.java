package com.tiny.rpc.codec;

import com.rpc.common.entity.RpcRequest;
import com.rpc.common.entity.RpcResponse;
import com.rpc.common.enumeration.PackageType;
import com.tiny.rpc.serializer.CommonSerializer;
import com.tiny.rpc.serializer.JsonSerializer;
import com.tiny.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

//Decoder：解码器
public class CommonDecoder extends ReplayingDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 1. 读魔数
        int magic = in.readInt();
        if (magic != 0xCAFEBABE) {
            throw new RuntimeException("不识别的协议包: " + magic);
        }

        // --- 【新增】读取包类型 ---
        int packageCode = in.readInt();
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            throw new RuntimeException("不识别的数据包类型: " + packageCode);
        }

        // 2. 读序列化器编号
        int serializerCode = in.readInt();
        //CommonSerializer serializer = new JsonSerializer(); // 暂时写死，后面用工厂获取
        //改为使用工厂
        CommonSerializer serializer = SerializerFactory.getByCode(serializerCode);

        // 3. 读数据长度
        int length = in.readInt();

        // 4. 读数据
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        // 5. 反序列化
        // --- 【修改】精准反序列化 ---
        Object obj = serializer.deserialize(bytes, packageClass);
        out.add(obj);
    }
}