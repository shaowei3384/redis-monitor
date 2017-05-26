package com.xijian.ecg.record.web.service;

import com.xijian.ecg.record.web.event.ConfigEvent;
import com.xijian.ecg.record.web.event.EventListener;
import com.xijian.ecg.record.web.life.LifeCycle;
import com.xijian.ecg.record.web.util.HessianUtil;
import com.xijian.ecg.record.web.util.LevelDbUtil;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 邵伟 on 2017/5/18 0018.
 * 配置管理
 */
public class ConfigManager implements LifeCycle{

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private List<EventListener> listeners = new ArrayList<EventListener>();

    private DB configDb;

    //取数据间隔 秒
    private int getInterval = 600 * 1000;

    public static String INTERVAL_NAME = "config_get_interval";

    private void init(){
        LOGGER.info("启动配置管理模块 <<<<<<<<<<<<<<<<<<<");
       try {
           configDb = LevelDbUtil.openConfigDb();
           byte[] val = LevelDbUtil.getMetric(configDb,INTERVAL_NAME);
           if(null == val){
               LevelDbUtil.addMetric(configDb,INTERVAL_NAME,HessianUtil.serialize(getInterval));
           }
           LOGGER.info("启动配置管理模块成功>>>>>>>>>>>>>>>>>");
       }catch (IOException e){
           LOGGER.error("启动配置管理模块异常>>>>>>>>>>>>>>>>",e);
           throw new RuntimeException("启动配置管理模块异常");
       }
    }

    /**
     * 注册配置变更监听
     * @param listener
     */
    public void registerListener(EventListener listener){
        this.listeners.add(listener);
    }

    public Integer getIntervalConfig(){
        LOGGER.info("从DB获取interval配置 <<<<<<<<<<<<<<<<<<<");
        byte[] bs = LevelDbUtil.getMetric(configDb,INTERVAL_NAME);
        LOGGER.info("从DB获取interval配置，结果是 {}>>>>>>>>>>>>>",bs);
        if(null == bs){
            return getInterval;
        }
        Integer val = getInterval;
        try{
            val = (Integer)HessianUtil.deserialize(bs);
        }catch (Exception e){
            //
        }
        return val;
    }

    public void removeConfig(String key){
        LOGGER.info("删除配置到配置管理，key is {},val is {} <<<<<<<<<<<<<<<<<<<",key);
        try{
            LevelDbUtil.removeMetric(configDb,key);
            LOGGER.info("删除配置到配置管理成功>>>>>>>>>>>>>>>>>>>>>>>");
        }catch (Exception e){
            LOGGER.error("删除配置到配置管理异常>>>>>>>>>>>>>>>>>>>>>>>",e);
        }
    }

    public <T> void addConfig(String key,T t){
        LOGGER.info("添加配置到配置管理，key is {},val is {} <<<<<<<<<<<<<<<<<<<",key,t);
        try{
            byte[] bs = HessianUtil.serialize(t);
            LevelDbUtil.addMetric(configDb,key,bs);
            LOGGER.info("添加配置到配置管理成功>>>>>>>>>>>>>>>>>>>>>>>");
        }catch (Exception e){
            LOGGER.error("添加配置到配置管理异常>>>>>>>>>>>>>>>>>>>>>>>",e);
        }
        //通知变更监听
        if(null != t){
            ConfigEvent event = new ConfigEvent();
            event.setName(key);
            event.setNewValue(t);
            for(EventListener listener : listeners){
                try{
                    listener.notify(event);
                }catch (Exception e){
                    LOGGER.error("数据变更通知监听器{}异常",listener,e);
                }
            }
        }
    }

    public <T> T getConfigObj(String key){
        LOGGER.info("从配置管理获取配置信息，key is {}<<<<<<<<<<<<<<<<<<<",key);
        byte[] bs = LevelDbUtil.getMetric(configDb,key);
        Object obj = null;
        try{
            if(bs == null){
                LOGGER.info("从配置管理获取配置信息，没有该项配置>>>>>>>>>>>>>>>>>>>>>");
                return null;
            }
            obj = HessianUtil.deserialize(bs);
            LOGGER.info("从配置管理获取配置信息，获取成功>>>>>>>>>>>>>>>>>>>>>");
        }catch (Exception e){
            LOGGER.error("从配置管理获取配置信息，获取异常>>>>>>>>>>>>>>>>>>>>>",e);
        }
        if(null == obj){
            return null;
        }
        return (T)obj;
    }

    public Map<String,Object> getConfigs(){
        Map<String,Object> map = new java.util.HashMap<String,Object>();
        try{
            map = LevelDbUtil.getConfigs(configDb);
        }catch (Exception e){
            LOGGER.error("获取配置列表异常",e);
        }
        return map;
    }

    @Override
    public void start() {
        init();
    }

    @Override
    public void shutdown() {
        try{
            configDb.close();
            configDb = null;
        }catch (Exception e){
            LOGGER.error("关闭配置管理模块异常>>>>>>>>>>>>>>>>>>>>>",e);
        }
    }
}
