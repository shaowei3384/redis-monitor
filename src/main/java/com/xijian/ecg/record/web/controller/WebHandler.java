package com.xijian.ecg.record.web.controller;
import com.xijian.ecg.record.web.config.SysConfig;
import com.xijian.ecg.record.web.service.FeatureManager;
import com.xijian.ecg.record.web.util.LevelDbUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class WebHandler {

    FeatureManager featureManager = FeatureManager.getInstance();

    private void init(String logfile,String confile) throws Exception{
        DOMConfigurator.configure(logfile);
        SysConfig sysConfig= new SysConfig(confile);
        Integer port = Integer.valueOf(sysConfig.getProperty("config.sys.service.port"));
        String basePath = sysConfig.getProperty("config.sys.service.basepath");
        String dataPath = sysConfig.getProperty("config.sys.data.basepath");
        LevelDbUtil.rootPath = dataPath;
        Server server = new Server(port);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setBaseResource(Resource.newClassPathResource(basePath));
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new RedisHostHandler(featureManager)});
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    public final static void main(String[] args){
        WebHandler webHandler = new WebHandler();
        if(null == args || args.length < 2){
            System.out.println("命令入参必须包含配置文件和log文件位置,eg:java -jar xxxx.jar log=log4j.xml config=system.properties");
            return;
        }
        String log = null;
        String conf = null;
        for(int i=0;i<args.length;i++){
            if(args[i].contains("log") && args[i].contains("=")){
                log = args[i].split("=")[1];
            }
            if(args[i].contains("config") && args[i].contains("=")){
                conf = args[i].split("=")[1];
            }
        }
        if(StringUtils.isEmpty(log) || StringUtils.isEmpty(conf)){
            System.out.println("命令入参必须包含配置文件和log文件位置,eg:java -jar xxxx.jar log=log4j.xml config=system.properties");
            return;
        }
        try{
            webHandler.init(log,conf);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
