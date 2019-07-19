package cn.wuxia.common.cached.redis;

import cn.wuxia.common.ProtostuffUtils;
import cn.wuxia.common.SerializeDeserializeWrapper;
import cn.wuxia.common.util.SerializeUtils;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectsTranscoder extends SerializeTranscoder {

    @Override
    public byte[] serialize(Object value) {
        if (value == null) {
            return null;
        }
        SerializeDeserializeWrapper wrapper = SerializeDeserializeWrapper.builder(value);
        return ProtostuffUtils.serialize(wrapper);
    }

    @Override
    public Object deserialize(byte[] serializeBytes) {
        if(serializeBytes == null){
            return null;
        }
        SerializeDeserializeWrapper deserializeWrapper = ProtostuffUtils.deserialize(serializeBytes, SerializeDeserializeWrapper.class);
        return deserializeWrapper.getData();
    }
}
