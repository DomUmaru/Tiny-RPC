package com.tiny.rpc.codec;

import com.rpc.common.entity.RpcRequest;
import com.rpc.common.enumeration.PackageType;
import com.tiny.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

//Encoder：编码器
public class CommonEncoder extends MessageToByteEncoder {

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 1. 写魔数 (4 bytes)
        out.writeInt(0xCAFEBABE);

        // 2. 写包类型 (这里简化处理，暂时不写，靠对象类型判断)
        // 实际生产中要写，比如 0=Request, 1=Response

        // --- 【新增】写入包类型 ---
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }

        // 3. 写序列化器编号 (4 bytes)
        out.writeInt(serializer.getCode());

        // 4. 序列化数据
        byte[] bytes = serializer.serialize(msg);

        // 5. 写数据长度 (4 bytes)
        out.writeInt(bytes.length);

        // 6. 写数据主体
        out.writeBytes(bytes);
    }
}