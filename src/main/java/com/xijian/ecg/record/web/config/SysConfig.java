package com.xijian.ecg.record.web.config;

import com.xijian.ecg.record.web.service.AlermManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by 邵伟 on 2017/5/22 0022.
 */
public class SysConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SysConfig.class);

    private Properties properties = new Properties();

    public SysConfig(String file){
        init(file);
    }

    private void init(String file){
        LOGGER.info("系统配置模块启动<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        try{
            properties.load(new FileInputStream(file));
        }catch (Exception e){
            LOGGER.error("系统配置模块启动异常>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",e);
            return;
        }
        LOGGER.info("系统配置模块启动成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    public String getProperty(String key){
        return this.properties.getProperty(key);
    }
}
