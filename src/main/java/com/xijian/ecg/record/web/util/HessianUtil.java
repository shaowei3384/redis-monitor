package com.xijian.ecg.record.web.util;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by 邵伟 on 2017/5/18 0018.
 */
public class HessianUtil {

    public final static byte[] serialize(Object obj) throws IOException{
        if(obj==null) throw new NullPointerException();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(os);
        ho.writeObject(obj);
        return os.toByteArray();
    }

    public final static Object deserialize(byte[] by) throws IOException{
        if(by==null) throw new NullPointerException();
        ByteArrayInputStream is = new ByteArrayInputStream(by);
        HessianInput hi = new HessianInput(is);
        return hi.readObject();
    }
}
