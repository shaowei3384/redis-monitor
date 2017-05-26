package com.xijian.ecg.record.web.service;

import com.xijian.ecg.record.web.domain.CmdStatDomain;
import com.xijian.ecg.record.web.domain.KeySpaceDomain;
import com.xijian.ecg.record.web.domain.RedisDomain;
import com.xijian.ecg.record.web.life.LifeCycle;
import com.xijian.ecg.record.web.util.HessianUtil;
import com.xijian.ecg.record.web.util.LevelDbUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 邵伟 on 2017/5/18 0018.
 */
public class MonitorManager implements LifeCycle{

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManager.class);

    private Map<String,DB> dbMap = new HashMap<String, DB>();

    private final static String SERVER_METRIC = "server";

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private Lock rlock = lock.readLock();

    private Lock wlock = lock.writeLock();

    /**
     * 添加打点数据
     * @param domain
     * @param metrics
     */
    public void addMetric(RedisDomain domain,Map<String, Object> metrics){
        LOGGER.info("添加metrics数据——〉DB,入参是 host={},metrics={}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<",domain,metrics);
        String time = DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        Set<String> set = new HashSet<String>();
        for(String key : metrics.keySet()){
            set.add(key);
        }
        for(String key : set){
            try{
                rlock.lock();
                DB db = getDB(metrics,key,domain);
                if(null == db){
                    continue;
                }
                if(key.startsWith("cmdstat")){
                    Object val = metrics.get(key);
                    if(null != val){
                        LevelDbUtil.addMetric(db,key = key.split("_")[1],HessianUtil.serialize(val));
                    }
                }else if(key.startsWith("db")){
                    LevelDbUtil.addMetric(db,key, HessianUtil.serialize(metrics.get(key)));
                }else if(servers.contains(key)){
                    LevelDbUtil.addMetric(db,key, HessianUtil.serialize(metrics.get(key)));
                }else {
                    LevelDbUtil.addMetric(db,time, HessianUtil.serialize(metrics.get(key)));
                }
                LOGGER.info("添加metrics数据——〉DB,添加 key={},val={} 到DB成功",key,metrics.get(key));
            }catch (IOException e){
                LOGGER.error("添加metrics数据——〉DB,添加 key={},val={} 到DB异常",key,metrics.get(key),e);
            }finally {
                rlock.unlock();
            }
        }
        //添加qps
        addQpsToDb(time,domain,metrics);
        //添加缓存命中率
        addCacheHitRateToDb(time,domain,metrics);

        LOGGER.info("添加metrics数据——〉DB结束>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
    }

    private void addCacheHitRateToDb(String time,RedisDomain domain,Map<String, Object> metrics){
        TreeMap<String,Double> result1 = (TreeMap)getMetrics(domain,"keyspace_hits");
        TreeMap<String,Double> result2 = (TreeMap)getMetrics(domain,"keyspace_misses");
        int size1 = result1.size();
        int size2 = result2.size();
        if(size1 >= 1 && size2 >= 1){
            LOGGER.info("开始添加CacheHitRate,time={},redis={}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<",time,domain.toString());
            Map.Entry<String,Double> last1 = result1.pollLastEntry();
            Map.Entry<String,Double> last2 = result2.pollLastEntry();
            try{
                BigDecimal cacheHitRate = new BigDecimal(last1.getValue() / (last1.getValue() + last2.getValue())).setScale(1,BigDecimal.ROUND_HALF_DOWN);
                cacheHitRate = cacheHitRate.multiply(new BigDecimal(100));
                LOGGER.info("计算CacheHitRate为{}",cacheHitRate.doubleValue());
                DB qpsDb = getDB("cache_hit_rate",domain);
                LevelDbUtil.addMetric(qpsDb,time,HessianUtil.serialize(cacheHitRate.doubleValue()));
                metrics.put("cache_hit_rate",cacheHitRate.doubleValue());
                LOGGER.info("添加CacheHitRate成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }catch (Exception e){
                LOGGER.error("根据keyspace_hits,keyspace_misses添加cache_hit_rate异常",e);
            }
        }
    }

    private void addQpsToDb(String time,RedisDomain domain,Map<String, Object> metrics){
        TreeMap<String,Double> result = (TreeMap)getMetrics(domain,"total_commands_processed");
        int size = result.size();
        if(size >= 2){
            LOGGER.info("开始添加qps,time={},redis={}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<",time,domain.toString());
            Map.Entry<String,Double> last = result.pollLastEntry();
            Map.Entry<String,Double> pre = result.pollLastEntry();
            try{
                Date lastDay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(last.getKey());
                Date preDay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(pre.getKey());
                long secend = (lastDay.getTime() - preDay.getTime()) / 1000;
                BigDecimal qps = new BigDecimal((last.getValue() - pre.getValue()) / secend).setScale(1,BigDecimal.ROUND_HALF_DOWN);
                LOGGER.info("计算qps为{}",qps.doubleValue());
                DB qpsDb = getDB("qps",domain);
                LevelDbUtil.addMetric(qpsDb,time,HessianUtil.serialize(qps.doubleValue()));
                metrics.put("qps",qps.doubleValue());
                LOGGER.info("添加qps成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }catch (Exception e){
                LOGGER.error("根据total_commands_processed添加qps异常",e);
            }
        }
    }
    /**
     * 获取打点数据
     * @param domain
     * @param metric
     * @param <T>
     * @return
     */
    public <T> Map<String,T> getMetrics(RedisDomain domain,String metric){
        LOGGER.info("查询metrics,入参是 host={},metric={}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<",domain,metric);
        Map<String,T> result = new TreeMap<String, T>();
        try{
            rlock.lock();
            DB db = getDB(null,metric,domain);
            Map<String,byte[]> map = LevelDbUtil.getMetrics(db);
            for(String key : map.keySet()){
                byte[] bs = map.get(key);
                result.put(key,(T)HessianUtil.deserialize(bs));
            }
        }catch (Exception e){
            LOGGER.error("查询metrics异常>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",e);
        }finally {
            rlock.unlock();
        }
        LOGGER.info("查询metrics,结果是 result={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",result.toString());
        return result;
    }

    //停止数据远服务
    public void shutdown(){
        try{
            wlock.lock();
            for(String key : dbMap.keySet()){
                dbMap.get(key).close();
            }
            dbMap.clear();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            wlock.unlock();
        }
    }

    //清空数据
    public void clean(){
        try{
            wlock.lock();
            shutdown();
            LevelDbUtil.cleanFile();
        }finally {
            wlock.unlock();
        }
    }

    @Override
    public void start() {
        try{
            wlock.lock();
            init();
        }finally {
            wlock.unlock();
        }
    }

    private DB getDB(String key,RedisDomain domain) throws IOException{
        DB db = null;
        if(key.startsWith("cmdstat")){
            if(key.contains("_")){
                key = key.split("_")[0];
            }else {
                key = "cmdstat";
            }
        }
        if(key.startsWith("db")){
            key = "db";
        }
        if((db = dbMap.get(domain.getIp()+domain.getPort()+key)) == null){
            db = LevelDbUtil.openMetricDb(domain.getIp() + domain.getPort(),key);
            dbMap.put(domain.getIp()+domain.getPort()+key,db);
        }
        return db;
    }
    private DB getDB(Map<String, Object> metrics,String key,RedisDomain domain) throws IOException{
        LOGGER.info("获取DB,入参是 host={},key={}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<",domain,key);
        DB db = null;
        if(null == metrics){
            metrics = new HashMap<String, Object>();
        }
        if(this.servers.contains(key)){
            db = getDB(SERVER_METRIC,domain);
            LOGGER.info("获取DB，key属于server，db={}.",db);
        }
        if(this.connnects.contains(key)){
            db = getDB(key,domain);
            LOGGER.info("获取DB，key属于connect，db={}.",db);
            Object val = metrics.get(key);
            if(null != val && val instanceof String){
                Integer value = Integer.valueOf(val.toString());
                LOGGER.info("获取DB，用 val={},替换掉当前key={}的值.",value,key);
                metrics.put(key,value);
            }
        }
        if(this.memorys.contains(key)){
            db = getDB(key,domain);
            LOGGER.info("获取DB，key属于memory，db={}.",db);
            Object val = metrics.get(key);
            if(null != val && val instanceof String){
                String v = val.toString().replaceAll("K","")
                        .replaceAll("M","")
                        .replaceAll("G","");
                Double value = Double.valueOf(v);
                metrics.put(key,value);
                LOGGER.info("获取DB，用 val={} 替换掉当前key={}的值.",value,key);
            }
        }
        if(this.aofs.contains(key)){
            db = getDB(key,domain);
            LOGGER.info("获取DB，key属于aof，db={}.",db);
        }
        if(this.cpus.contains(key)){
            db = getDB(key,domain);
            LOGGER.info("获取DB，key属于cpu，db={}.",db);
            Object val = metrics.get(key);
            if(null != val && val instanceof String){
                Double value = Double.valueOf(val.toString());
                metrics.put(key,value);
                LOGGER.info("获取DB，用 val={} 替换掉当前key={}的值.",value,key);
            }
        }
        if(key.matches(this.keyspaces.get(0))){
            db = getDB(key,domain);
            LOGGER.info("获取DB，key属于keyspaces，db={}.",db);
            Object val = metrics.get(key);
            if(null != val && val instanceof String){
                KeySpaceDomain keySpaceDomain = new KeySpaceDomain();
                keySpaceDomain.setData(val.toString());
                LOGGER.info("获取DB，用 value={} 替换掉 key={}的值.",keySpaceDomain.toString(),key);
                metrics.put(key,keySpaceDomain);
            }
        }
        if(this.statss.contains(key)){
            db = getDB(key,domain);
            LOGGER.info("获取DB，key属于stats，db={}.",db);
            Object val = metrics.get(key);
            if(null != val && val instanceof String){
                Double value = Double.valueOf(val.toString());
                LOGGER.info("获取DB，用 value={} 替换掉 key={} 的值.",value,key);
                metrics.put(key,value);
            }
        }
        if(key.contains(this.commondStats.get(0))){
            db = getDB(key,domain);
            LOGGER.info("获取DB，key属于commondStat，db={}.",db);
            Object val = metrics.get(key);
            if(null != val && val instanceof String){
                CmdStatDomain value = new CmdStatDomain();
                value.setData(key,val.toString());
                LOGGER.info("获取DB，用 value={} 替换掉　key={}　的值.",value.toString(),key);
                metrics.put(key,value);
            }
        }
        LOGGER.info("获取DB,出参是 db={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",db == null ? null : db.toString());
        return db;
    }

    private void init(){

    }

    private List<String> servers = new ArrayList<String>();

    private List<String> connnects = new ArrayList<String>();

    private List<String> memorys = new ArrayList<String>();

    private List<String> aofs = new ArrayList<String>();

    private List<String> cpus = new ArrayList<String>();

    private List<String> keyspaces = new ArrayList<String>();

    private List<String> statss = new ArrayList<String>();

    private List<String> commondStats = new ArrayList<String>();

    public MonitorManager(){
        //os
        servers.add("redis_version");
        servers.add("os");
        servers.add("arch_bits");
        servers.add("process_id");
        servers.add("tcp_port");
        servers.add("uptime_in_seconds");
        servers.add("uptime_in_days");
        servers.add("server");

        //connnect
        connnects.add("connected_clients");
        connnects.add("blocked_clients");

        //memory
        memorys.add("used_memory_human");
        memorys.add("used_memory_peak_human");
        memorys.add("mem_fragmentation_ratio");

        //aof
        aofs.add("aof_enabled");
        aofs.add("bgsave_in_progress");
        aofs.add("last_save_time");
        aofs.add("bgrewriteaof_in_progress");
        aofs.add("total_connections_received");
        aofs.add("total_commands_processed");

        //cpu
        cpus.add("used_cpu_sys");
        cpus.add("used_cpu_user");
        cpus.add("used_cpu_sys_children");
        cpus.add("used_cpu_user_children");

        //keyspace
        keyspaces.add("db[0-9]{0,}");

        //Stats
        statss.add("total_connections_received");
        statss.add("total_commands_processed");
        statss.add("qps");
        statss.add("cache_hit_rate");
        statss.add("instantaneous_ops_per_sec");
        statss.add("rejected_connections");
        statss.add("expired_keys");
        statss.add("evicted_keys");
        statss.add("keyspace_hits");
        statss.add("keyspace_misses");

        //commondStats
        commondStats.add("cmdstat");
    }
}
