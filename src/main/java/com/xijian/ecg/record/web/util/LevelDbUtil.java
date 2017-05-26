package com.xijian.ecg.record.web.util;

import com.xijian.ecg.record.web.domain.AlermItem;
import com.xijian.ecg.record.web.domain.CmdStatDomain;
import com.xijian.ecg.record.web.service.AlermManager;
import org.apache.commons.io.FileUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 邵伟 on 2017/5/18 0018.
 */
public class LevelDbUtil {

    private final static String CHARSET = "UTF-8";

    private final static String CONFIG_PATH = "config";

    private final static String ALERM_PATH = "alerm";

    private final static Object clock = new Object();

    public static String rootPath = ".";

    public final static DB openAlermDb() throws IOException{
        synchronized (clock){
            Options options = new Options();
            options.createIfMissing(true);
            DB db = Iq80DBFactory.factory.open(new File(rootPath + File.separator + ALERM_PATH),options);
            return db;
        }
    }

    public final static DB openConfigDb() throws IOException{
        synchronized (clock){
            Options options = new Options();
            options.createIfMissing(true);
            DB db = Iq80DBFactory.factory.open(new File(rootPath + File.separator + CONFIG_PATH),options);
            return db;
        }
    }

    public final static DB openMetricDb(String key,String metric) throws IOException{
        synchronized (clock){
            Options options = new Options();
            options.createIfMissing(true);
            DB db = Iq80DBFactory.factory.open(new File(rootPath + File.separator + key + File.separator + metric),options);
            return db;
        }
    }

    public final static void addAlerm(DB db, String mertric, AlermItem item)throws IOException{
        synchronized (clock){
            db.put(bytes(mertric),HessianUtil.serialize(item));
        }
    }

    public final static List<AlermItem> getAlerms(DB db)throws IOException{
        List<AlermItem> list = new ArrayList<AlermItem>();
        synchronized (clock){
            DBIterator iterator = db.iterator();
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                Object val = HessianUtil.deserialize(iterator.peekNext().getValue());
                list.add((AlermItem)val);
            }
        }
        return list;
    }

    public final static void cleanFile(){
        File f = new File(rootPath);
        File[] fs = f.listFiles();
        if(null != fs){
            for(File file : fs){
                if(file.isDirectory()
                        && file.getName().matches("^[0-9].*")){
                    try{
                        FileUtils.cleanDirectory(file);
                        file.delete();
                    }catch (Exception e){
                        //
                    }
                }
            }
        }
    }

    public final static List<String> queryMetricList(String key){
        List<String> list = new ArrayList<String>();
        File file = new File(rootPath + File.separator + key);
        if(file.exists() && file.isDirectory()){
            File[] fs = file.listFiles();
            if(null != fs){
                for(File f : fs){
                    list.add(f.getName());
                }
            }
        }
        return list;
    }

    public final static void removeMetric(DB db,String key){
        synchronized (clock){
            db.delete(bytes(key));
        }
    }
    public final static void addMetric(DB db,String key,byte[] val){
        synchronized (clock){
            db.put(bytes(key),val);
        }
    }

    public final static byte[] getMetric(DB db,String key){
        synchronized (clock){
            byte[] bs = db.get(bytes(key));
            return bs;
        }
    }

    public final static Map<String,Object> getConfigs(DB db) throws IOException{
        synchronized (clock){
            Map<String,Object> map = new HashMap<String,Object>();
            DBIterator iterator = db.iterator();
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                Object val = HessianUtil.deserialize(iterator.peekNext().getValue());
                map.put(key,val);
            }
            return map;
        }
    }

    public final static Map<String,byte[]> getMetrics(DB db){
        synchronized (clock){
            Map<String,byte[]> config = new HashMap<String, byte[]>();
            DBIterator iterator = db.iterator();
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                byte[] value = iterator.peekNext().getValue();
                if(null != value){
                    config.put(key,value);
                }
            }
            return config;
        }
    }

    public final static void close(DB db) throws IOException{
        synchronized (clock){
            db.close();
        }
    }

    private final static byte[] bytes(String param){
        try{
            return param.getBytes(CHARSET);
        }catch (Exception e){
            return null;
        }
    }

    private final static String asString(byte[] bs){
        try {
            return new String(bs,CHARSET);
        }catch (Exception e){
            return null;
        }
    }
}