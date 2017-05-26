package com.xijian.ecg.record.web.util;

import com.xijian.ecg.record.web.domain.AlermItem;
import com.xijian.ecg.record.web.service.AlermManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class JacksonUtil {

    public static final String toJson(Object obj){
        if (null == obj){
            return "{}";
        }
        ObjectMapper om = new ObjectMapper();
        String str = "{}";
        try{
            str = om.writeValueAsString(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
        return str;
    }

    public static final <T> T toBean(String json,Class<T> cls){
        ObjectMapper om = new ObjectMapper();
        T t = null;
        try{
            t = om.readValue(json,cls);
        }catch (Exception e){
            e.printStackTrace();
        }
        return t;
    }

    public static final <T> List<T> toList(String json, Class<T> cls){
        ObjectMapper om = new ObjectMapper();
        JavaType type = om.getTypeFactory().constructParametricType(List.class, cls);
        List<T> t = null;
        try{
            t = om.readValue(json,type);
        }catch (Exception e){
            e.printStackTrace();
        }
        return t;
    }
}
