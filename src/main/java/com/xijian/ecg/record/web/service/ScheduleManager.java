package com.xijian.ecg.record.web.service;

import com.xijian.ecg.record.web.domain.RedisDomain;
import com.xijian.ecg.record.web.event.*;
import com.xijian.ecg.record.web.life.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class ScheduleManager implements LifeCycle, com.xijian.ecg.record.web.event.EventListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleManager.class);

    private static final String CLEAN_METRIC_TIME_CONFIG = "clean_task_last_day";

    private List<Task> taskList = Collections.synchronizedList(new ArrayList<Task>());

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1000);

    private Cleartask cleanTask;

    private AlermManager alermManager;

    private ConfigManager configManager;

    private MonitorManager monitorManager;

    public void notify(ConfigEvent event) {
        LOGGER.info("接到配置管理模块数据变更通知 event:{}<<<<<<<<<<<<<<<<<<<<<<",event.toString());
        String name = event.getName();
        if(CLEAN_METRIC_TIME_CONFIG.equals(name)){
            cleanTask.setLastDay(Integer.valueOf((String)event.getNewValue()));
            LOGGER.info("配置管理模块处理变更，将clean任务的LastDay修改为{}",cleanTask.getLastDay());
        }
        if(ConfigManager.INTERVAL_NAME.equals(name)){
            for(Task t : taskList){
                t.setInterval(Integer.valueOf((String)event.getNewValue()));
                LOGGER.info("配置管理模块处理变更，将{}_task任务的Interval修改为{}",
                        t.getRedisDomain().getIp() + t.getRedisDomain().getPort(),event.getNewValue());
            }
        }
        if(FeatureManager.REIDS_HOST_CONFIG.equals(name)){
            synchronized (this){
                List<RedisDomain> list = (List<RedisDomain>)event.getNewValue();
                Iterator<Task> it = taskList.iterator();
                while (it.hasNext()){
                    Task t  = it.next();
                    RedisDomain domain = t.getRedisDomain();
                    boolean isHas = false;
                    for(RedisDomain redisDomain : list){
                        if(redisDomain.getIp().equals(domain.getIp()) && redisDomain.getPort().intValue() == domain.getPort().intValue()){
                            isHas = true;
                        }
                    }
                    if(!isHas){
                        t.setStop(true);
                        it.remove();
                        t.getRedisExecute().shutdown();
                        LOGGER.info("配置管理模块处理变更，将{}_task任务停止", t.getRedisDomain().getIp() + t.getRedisDomain().getPort());
                    }
                }
                for(RedisDomain redisDomain : list){
                    boolean isHash = false;
                    for(Task t : taskList){
                        RedisDomain domain = t.getRedisDomain();
                        if(redisDomain.getIp().equals(domain.getIp()) && redisDomain.getPort().intValue() == domain.getPort().intValue()){
                            isHash = true;
                        }
                    }
                    if(!isHash){
                        addTask(redisDomain,configManager.getIntervalConfig());
                        LOGGER.info("配置管理模块处理变更，添加{}_task任务", redisDomain.getIp() + redisDomain.getPort());
                    }
                }
            }
        }
        LOGGER.info("配置管理模块数据变更通知处理完成>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    public void start() {
        LOGGER.info("启动调度模块开始<<<<<<<<<<<<<<<<<<<<<<<");
        if(null != ScheduleManager.this.configManager){
            Integer lastDay = ScheduleManager.this.configManager.getConfigObj(CLEAN_METRIC_TIME_CONFIG);
            if(null == lastDay){
                lastDay = new Date().getDate();
                ScheduleManager.this.configManager.addConfig(CLEAN_METRIC_TIME_CONFIG,lastDay);
            }
            cleanTask = new Cleartask(lastDay);
        }else {
            cleanTask = new Cleartask();
        }
        new Thread(cleanTask,"monitor-clean-data-thread").start();
        LOGGER.info("启动调度模块成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    public void shutdown() {
        for(Task t : taskList){
            t.setStop(true);
        }
        if(null != cleanTask){
            cleanTask.setStop(true);
        }
        executor.shutdown();
    }

    public void setAlermManager(AlermManager alermManager) {
        this.alermManager = alermManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
        LOGGER.info("开始注册自身{}到配置管理数据变成监听器<<<<<<<<<<<<<<<<<<<<<",this);
        configManager.registerListener(this);
        LOGGER.info("注册自身{}到配置管理数据变成监听器成功>>>>>>>>>>>>>>>>>>>",this);
    }

    public void setMonitorManager(MonitorManager monitorManager) {
        this.monitorManager = monitorManager;
    }

    /**
     * 添加任务
     * @param redisDomain
     * @param interval
     */
    private void addTask(RedisDomain redisDomain,Integer interval){
        LOGGER.info("添加监控任务，入参是 host={},interval={}<<<<<<<<<<<<<<<<<<<<<<<",redisDomain,interval);
        synchronized (this){
            Task task = new Task(redisDomain,interval);
            this.taskList.add(task);
            executor.execute(task);
        }
        LOGGER.info("添加监控任务成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    private void removeTask(String ip,int port){
        LOGGER.info("删除监控任务，入参是 ip={},port={}<<<<<<<<<<<<<<<<<<<<<<<",ip,port);
        synchronized (this){
            Iterator<Task> it = taskList.iterator();
            while (it.hasNext()){
                Task t  = it.next();
                RedisDomain domain = t.getRedisDomain();
                if(ip.equals(domain.getIp()) && port == domain.getPort().intValue()){
                    t.setStop(true);
                    it.remove();
                    t.getRedisExecute().shutdown();
                }
            }
        }
        LOGGER.info("删除监控任务成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    public RedisExecute getExecute(String ip,Integer port){
        LOGGER.info("获取redis执行器，入参是 ip={},port={}<<<<<<<<<<<<<<<<<<<<<<<",ip,port);
        synchronized (this){
            for(Task t : taskList){
                RedisDomain domain = t.getRedisDomain();
                if(ip.equals(domain.getIp()) && port.intValue() == domain.getPort()){
                    LOGGER.info("获取redis执行器成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    return t.getRedisExecute();
                }
            }
        }
        LOGGER.info("获取redis执行器失败>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        return null;
    }

    class Cleartask implements Runnable {

        private boolean isStop;

        private Integer lastDay;

        public Cleartask(){
            this(null);
        }

        public Cleartask(Integer lastDay){
            this.lastDay = lastDay;
        }

        @Override
        public void run() {
            while (!isStop){
               try{
                   Date now = new Date();
                   LOGGER.info("开始判断清空监控DB.");
                   if(null == lastDay || now.getDate() != lastDay){
                       LOGGER.info("需要清空监控DB.");
                       ScheduleManager.this.monitorManager.clean();
                       if(ScheduleManager.this.configManager != null){
                           ScheduleManager.this.configManager.addConfig(CLEAN_METRIC_TIME_CONFIG,now.getDate());
                       }
                       lastDay = now.getDate();
                       LOGGER.info("清空监控DB成功.");
                   }
                   LOGGER.info("判断清空监控DB结束.");
                   Thread.sleep(60 * 60 * 1000);
               }catch (InterruptedException e) {
                   return;
               }
               catch (Exception e){
                   e.printStackTrace();
                   continue;
               }
            }
        }
        public boolean isStop() {
            return isStop;
        }

        public void setStop(boolean stop) {
            isStop = stop;
        }

        public Integer getLastDay() {
            return lastDay;
        }

        public void setLastDay(Integer lastDay) {
            this.lastDay = lastDay;
        }
    }

    class Task implements Runnable{

        private boolean isStop;

        private Integer interval;

        private RedisDomain redisDomain;

        private RedisExecute redisExecute;

        public Task(RedisDomain redisDomain,Integer interval){
            this.redisDomain = redisDomain;
            this.interval = interval;
            this.redisExecute = new RedisExecute();
            this.redisExecute.init(redisDomain);
        }

        @Override
        public void run() {
            while (!isStop){
                try{
                    Map<String,Object> map = this.redisExecute.getInfo();
                    if(null != map){
                        ScheduleManager.this.monitorManager.addMetric(redisDomain,map);
                    }
                    if(null != alermManager){
                        alermManager.alermInvoke(redisDomain,map);
                    }
                    Thread.sleep(interval);
                }catch (InterruptedException e){
                    ScheduleManager.this.taskList.remove(this);
                    return;
                } catch (Exception e){
                    e.printStackTrace();
                    try{
                        Thread.sleep(interval);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                }
            }
        }

        public void setInterval(Integer interval) {
            this.interval = interval;
        }

        public RedisDomain getRedisDomain() {
            return redisDomain;
        }

        public void setRedisDomain(RedisDomain redisDomain) {
            this.redisDomain = redisDomain;
        }

        public boolean isStop() {
            return isStop;
        }

        public void setStop(boolean stop) {
            isStop = stop;
        }

        public RedisExecute getRedisExecute() {
            return redisExecute;
        }

        public void setRedisExecute(RedisExecute redisExecute) {
            this.redisExecute = redisExecute;
        }
    }
}
