package com.xijian.ecg.record.web.service;

import com.xijian.ecg.record.web.domain.AlermItem;
import com.xijian.ecg.record.web.domain.RedisDomain;
import com.xijian.ecg.record.web.event.*;
import com.xijian.ecg.record.web.life.LifeCycle;
import com.xijian.ecg.record.web.util.LevelDbUtil;
import com.xijian.ecg.record.web.util.SendEmailUtils;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by 邵伟 on 2017/5/21 0021.
 * 预警模块
 */
public class AlermManager implements LifeCycle, com.xijian.ecg.record.web.event.EventListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(AlermManager.class);

    private DB alermDb;

    private JavaMailSenderImpl mailSender;

    private String host;

    private Integer port;

    private String[] toUser;

    private String fromUser;

    private String fromPwd;

    private boolean isStart;

    private ConfigManager configManager;

    private Map<String,AlermItem> availableItems = new HashMap<String, AlermItem>();

    public AlermManager(ConfigManager configManager){
        Assert.notNull(configManager);
        this.configManager = configManager;
        LOGGER.info("开始注册自身{}到配置管理数据变成监听器<<<<<<<<<<<<<<<<<<<<<",this);
        configManager.registerListener(this);
        LOGGER.info("注册自身{}到配置管理数据变成监听器成功>>>>>>>>>>>>>>>>>>>",this);
    }

    public void notify(ConfigEvent event) {
        LOGGER.info("接到配置管理模块数据变更通知 event:{}<<<<<<<<<<<<<<<<<<<<<<",event.toString());
        String name = event.getName();
        if("smtp.host".equals(name)){
            String host = (String)event.getNewValue();
            Assert.hasLength(host,"Alerm host is null");
            this.host = host;
        }
        if("smtp.port".equals(name)){
            int port = Integer.valueOf((String)event.getNewValue());
            Assert.isTrue(port > 0,"Alerm port is null");
            this.port = port;
        }
        if("smtp.fromUser".equals(name)){
            String fromUser = (String)event.getNewValue();
            Assert.hasLength(fromUser,"Alerm fromUser is null");
            this.fromUser = fromUser;
        }
        if("smtp.frompwd".equals(name)){
            String fromPwd = (String)event.getNewValue();
            Assert.hasLength(fromPwd,"Alerm fromPwd is null");
            this.fromPwd = fromPwd;
        }
        if("smtp.toUser".equals(name)){
            String[] toUser = (String[])event.getNewValue();
            Assert.notEmpty(toUser,"Alerm toUser is null");
            this.toUser = toUser;
        }
        try{
            Assert.hasLength(host,"Alerm host is null");
            Assert.isTrue(port > 0,"Alerm port is null");
            Assert.hasLength(fromUser,"Alerm fromUser is null");
            Assert.hasLength(fromPwd,"Alerm fromPwd is null");
            Assert.notEmpty(toUser,"Alerm toUser is null");
            isStart = true;
            LOGGER.info("满足告警模块配置，启动报警模块");
        }catch (Exception e){
            //
        }
        LOGGER.info("配置管理模块数据变更通知处理完成>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    public void start() {
        LOGGER.info("开始启动预警模块<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        try{
            String host = this.configManager.getConfigObj("smtp.host");
            int port = Integer.valueOf(this.configManager.getConfigObj("smtp.port").toString());
            String fromUser = this.configManager.getConfigObj("smtp.fromUser");
            String fromPwd = this.configManager.getConfigObj("smtp.frompwd");
            String[] toUser = this.configManager.getConfigObj("smtp.toUser");
            Assert.hasLength(host,"Alerm host is null");
            Assert.isTrue(port > 0,"Alerm port is null");
            Assert.hasLength(fromUser,"Alerm fromUser is null");
            Assert.hasLength(fromPwd,"Alerm fromPwd is null");
            Assert.notEmpty(toUser,"Alerm toUser is null");
            this.host = host;
            this.fromPwd = fromPwd;
            this.port = port;
            this.fromUser = fromUser;
            this.toUser = toUser;
            isStart = true;
        }catch (Exception e){
            LOGGER.error("启动预警模块失败,原因是:{}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",e.getMessage());
            return;
        }
        LOGGER.info("启动预警模块成功>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    public void shutdown() {
        try{
            if(!isStart){
                return;
            }
            if(null != alermDb){
                alermDb.close();
                alermDb = null;
            }
        }catch (Exception e){
            //
        }
    }

    public void alermInvoke(RedisDomain redisDomain, Map<String, Object> metrics)throws IOException{
        if(!isStart){
            return;
        }
        LOGGER.info("进入告警模块,进行预警检查<<<<<<<<<<<<<<<<<<<<<<<");
        List<AlermItem> list = getAlerms();
        updateAvailableItem(redisDomain,metrics,list);
        try{
            Assert.notEmpty(metrics);
        }catch (Exception e){
            for(AlermItem item : list){
                if(item.getIsUse() && !item.getIsNull() && item.getName().equals(redisDomain.getIp() + redisDomain.getPort())){
                    //预警
                    sendAlermEmail(redisDomain.getIp() + redisDomain.getPort() + "长时间未获取到redis监控数据");
                }
            }
        }
        for(AlermItem item : list){
            try{
                Object val = metrics.get(item.getName());
                if(null == val){
                    continue;
                }
                if(val instanceof Number){
                    Number number = (Number)val;
                    double value = number.doubleValue();
                    if(item.getMaxValue() != null && value > item.getMaxValue().doubleValue()  && item.getIsUse()){
                        item.setCurrentValue(value);
                        throw new AlermException(item);
                    }
                }
            }catch (AlermException e){
                //预警
                sendAlermEmail(redisDomain.getIp() + redisDomain.getPort() + e.getMessage());
            }
        }
        LOGGER.info("告警模块检查结束>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>...");
    }

    public void addAlerm(AlermItem item)throws IOException{
        if(!isStart){
            return;
        }
        Assert.notNull(item,"item must be not null");
        Assert.hasLength(item.getName(),"name must be not null");
        DB db = getAlermDb();
        LevelDbUtil.addAlerm(db,item.getName(),item);
        availableItems.put(item.getName(),item);
    }

    public Collection<AlermItem> queryAlermList(){
        if(!isStart){
            return Collections.emptyList();
        }
        List<AlermItem> list = new ArrayList(availableItems.values());
        Collections.sort(list, new Comparator<AlermItem>() {
            @Override
            public int compare(AlermItem o1, AlermItem o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return Collections.unmodifiableCollection(list);
    }

    private List<AlermItem> getAlerms()throws IOException{
        DB db = getAlermDb();
        return LevelDbUtil.getAlerms(db);
    }

    private void sendAlermEmail(String msg){
        SendEmailUtils sendEmailUtils = new SendEmailUtils();
        sendEmailUtils.setAccount(fromUser);
        sendEmailUtils.setPass(fromPwd);
        sendEmailUtils.setFrom(fromUser);
        sendEmailUtils.setHost(host);
        sendEmailUtils.setPort(String.valueOf(port));
        sendEmailUtils.setProtocol("smtp");
        sendEmailUtils.setTo(toUser);
        sendEmailUtils.send("Redis监控预警",msg);
    }

    private void updateAvailableItem(RedisDomain redisDomain,Map<String, Object> metrics,List<AlermItem> list){
        metrics.put(redisDomain.getIp()+redisDomain.getPort(),1000);
        if(!CollectionUtils.isEmpty(metrics)){
            for(String key : metrics.keySet()){
                Object val = metrics.get(key);
                AlermItem alermItem = null;
                LOGGER.info("添加预警项判断,入参是 key={},val={}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<",key,val == null ? val : val.toString());
                if(null != val && val instanceof Number){
                    for(AlermItem item : list){
                        if(item.getName().equals(key)){
                            alermItem = item;
                            break;
                        }
                    }
                    if(null == alermItem){
                        AlermItem item = new AlermItem();
                        item.setName(key);
                        availableItems.put(key,item);
                    }else {
                        availableItems.put(key,alermItem);
                    }
                    LOGGER.info("添加预警项判断,添加成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                } else {
                    LOGGER.info("添加预警项判断,val不是数字，不用添加>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
            }
        }
    }

    private DB getAlermDb()throws IOException{
        if(null == alermDb){
            alermDb = LevelDbUtil.openAlermDb();
        }
        return alermDb;
    }

    class AlermException extends Exception{

        private final static String alermText = "预警项 {0} 超过阙值{1},现值为{2},触发预警";

        public AlermException(AlermItem item){
            this(MessageFormat.format(alermText,item.getName(),item.getMaxValue(),item.getCurrentValue()));
        }
        private AlermException(String messge){
            super(messge);
        }
    }
}
