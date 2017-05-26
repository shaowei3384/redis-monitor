package com.xijian.ecg.record.web.service;

import com.xijian.ecg.record.web.domain.AlermItem;
import com.xijian.ecg.record.web.domain.ClientDomain;
import com.xijian.ecg.record.web.domain.RedisDomain;
import com.xijian.ecg.record.web.domain.SlowLogDomain;
import com.xijian.ecg.record.web.life.LifeCycle;
import com.xijian.ecg.record.web.result.Response;
import com.xijian.ecg.record.web.result.ResponseData;
import com.xijian.ecg.record.web.util.JacksonUtil;
import com.xijian.ecg.record.web.util.LevelDbUtil;
import com.xijian.ecg.record.web.util.PageUtil;
import com.xijian.ecg.record.web.vo.PageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 * 功能模块
 */
public class FeatureManager implements LifeCycle{

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    private ConfigManager configManager;

    private ScheduleManager scheduleManager;

    private MonitorManager monitorManager;

    private AlermManager alermManager;

    private boolean isInit;

    public final static String REIDS_HOST_CONFIG = "reids_host_list";

    private static FeatureManager instance = new FeatureManager();

    private FeatureManager(){

    }

    public static FeatureManager getInstance(){
        return instance;
    }

    @Override
    public void start() {
        LOGGER.info("开始启动feature模块<<<<<<<<<<<<<<<<<<<<<<<<<<");
        configManager = new ConfigManager();
        configManager.start();
        monitorManager = new MonitorManager();
        monitorManager.start();
        alermManager = new AlermManager(configManager);
        alermManager.start();
        scheduleManager = new ScheduleManager();
        scheduleManager.setMonitorManager(monitorManager);
        scheduleManager.setAlermManager(alermManager);
        scheduleManager.setConfigManager(configManager);
        scheduleManager.start();
        LOGGER.info("开始启动feature模块,查询需监控的redis机器<<<<<<<<<<<<<<<<<<<<<<<<<<");
        List<RedisDomain> list = configManager.getConfigObj(REIDS_HOST_CONFIG);
        LOGGER.info("开始启动feature模块,查询需监控的redis机器结果是{}>>>>>>>>>>>>>>>>>>>>>",null == list ? null : list.toString());
        if(null != list && list.size() > 0){
            configManager.addConfig(REIDS_HOST_CONFIG,list);
        }
        isInit = true;
        LOGGER.info("启动feature模块成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    public void shutdown() {
        scheduleManager.shutdown();
        monitorManager.shutdown();
        configManager.shutdown();
        isInit = false;
    }

    public boolean isInit(){
        return isInit;
    }

    public Response<String> queryMetrics(String ip, Integer port){
        LOGGER.info("查询metric，入参是 ip={},prot={}<<<<<<<<<<<<<<<<<<<<<<<<<<",ip,port);
        Response<String> rsp = new Response<String>();
        List<String> list = LevelDbUtil.queryMetricList(ip + port.toString());
        rsp.setRows(list);
        LOGGER.info("查询metric，结果是 list={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",list.toString());
        return rsp;
    }

    public ResponseData<Map> queryConfigList(){
        LOGGER.info("查询configList<<<<<<<<<<<<<<<<<<<<<<<<<<");
        ResponseData<Map> rsp = new ResponseData<Map>();
        Map map = configManager.getConfigs();
        rsp.setData(map);
        LOGGER.info("查询configList,结果是 list={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",map.toString());
        return rsp;
    }

    public Response removeConfig(String key){
        LOGGER.info("删除config,参数是 key={}<<<<<<<<<<<<<<<<<<<<<<<<<<",key);
        Assert.hasLength(key,"key must be not null");
        Response rsp = new Response();
        configManager.removeConfig(key);
        LOGGER.info("删除config,结果是 rsp={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",rsp.toString());
        return rsp;
    }

    public Response adddConfig(String key,String value){
        LOGGER.info("添加config,参数是 key={},value={}<<<<<<<<<<<<<<<<<<<<<<<<<<",key,value);
        Assert.hasLength(key,"key must be not null");
        Assert.hasLength(value,"value must be not null");
        Response rsp = new Response();
        if(key.equals("smtp.toUser")){
            String[] val = value.split(",");
            configManager.addConfig(key,val);
        }else {
            configManager.addConfig(key,value);
        }
        LOGGER.info("添加config,结果是 rsp={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",rsp.toString());
        return rsp;
    }

    /**
     * 查询打点数据
     * @param ip
     * @param port
     * @param metric
     * @return
     */
    public ResponseData<Object> queryMetrics(String ip, Integer port, String metric){
        LOGGER.info("查询metric,入参是 ip={},port={},metric={}<<<<<<<<<<<<<<<<<<<<<<<<<<",ip,port,metric);
        RedisDomain redisDomain = new RedisDomain();
        redisDomain.setIp(ip);
        redisDomain.setPort(port);
        Map<String,Object> map = monitorManager.getMetrics(redisDomain,metric);
        ResponseData rsp = new ResponseData();
        rsp.setData(map);
        LOGGER.info("查询metric, 结果是 map={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",map.toString());
        return rsp;
    }

    /**
     * 获取慢查日志
     * @param ip
     * @param port
     * @param vo
     * @return
     */
    public Response<SlowLogDomain> querySlowLog(String ip,Integer port,PageVo vo){
        LOGGER.info("查询slowlog,入参是 ip={},port={},page={}<<<<<<<<<<<<<<<<<<<<<<<<<<",ip,port,vo.toString());
        RedisExecute execute = scheduleManager.getExecute(ip,port);
        Response<SlowLogDomain> rsp = new Response<SlowLogDomain>();
        if(null != execute){
            List<SlowLogDomain> list = execute.getSlowLog();
            if(!CollectionUtils.isEmpty(list)){
                Collections.sort(list, new Comparator<SlowLogDomain>() {
                    @Override
                    public int compare(SlowLogDomain o1, SlowLogDomain o2) {
                        return (int)(o1.getExecutionTime() - o2.getExecutionTime());
                    }
                });
            }
            rsp = PageUtil.page(list,vo);
        }
        LOGGER.info("查询slowlog,结果是 rsp={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",rsp.toString());
        return rsp;
    }

    /**
     * 获取客户端连接list
     * @param ip
     * @param port
     * @param vo
     * @return
     */
    public Response<ClientDomain> queryClientList(String ip, Integer port, PageVo vo){
        LOGGER.info("查询ClientList,入参是 ip={},port={},page={}<<<<<<<<<<<<<<<<<<<<<<<<<<",ip,port,vo.toString());
        RedisExecute execute = scheduleManager.getExecute(ip,port);
        Response<ClientDomain> rsp = new Response<ClientDomain>();
        if(null != execute){
            List<ClientDomain> list = execute.getClientList();
            if(!CollectionUtils.isEmpty(list)){
                Collections.sort(list, new Comparator<ClientDomain>() {
                    @Override
                    public int compare(ClientDomain o1, ClientDomain o2) {
                        return o1.getAddr().compareTo(o2.getAddr());
                    }
                });
            }
            rsp = PageUtil.page(list,vo);
        }
        LOGGER.info("查询ClientList,结果是 rsp={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>",rsp);
        return rsp;
    }

    /**
     * 添加监控主机
     * @param ip
     * @param port
     * @param passwd
     */
    public void addRedisHost(String ip,Integer port,String passwd){
        LOGGER.info("添加redisHost,入参是 ip={},port={},pwd={}<<<<<<<<<<<<<<<<<<<<<<<<<<",ip,port,passwd);
        RedisDomain redisDomain = new RedisDomain();
        redisDomain.setIp(ip);
        redisDomain.setPort(port);
        redisDomain.setPasswd(passwd);
        List<RedisDomain> list = configManager.getConfigObj(REIDS_HOST_CONFIG);
        if(null == list){
            list = new ArrayList<RedisDomain>();
        }
        list.add(redisDomain);
        configManager.addConfig(REIDS_HOST_CONFIG,list);
        LOGGER.info("添加redisHost成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    /**
     * 删除监控主机
     * @param ip
     * @param port
     */
    public void removeRedisHost(String ip,Integer port){
        LOGGER.info("删除redisHost,入参是 ip={},port={}<<<<<<<<<<<<<<<<<<<<<<<<<<",ip,port);
        List<RedisDomain> list = configManager.getConfigObj(REIDS_HOST_CONFIG);
        if(null == list){
            list = new ArrayList<RedisDomain>();
        }
        Iterator<RedisDomain> it = list.iterator();
        while(it.hasNext()){
            RedisDomain domain = it.next();
            if(ip.equals(domain.getIp()) && port.intValue() == domain.getPort().intValue()){
                it.remove();
            }
        }
        configManager.addConfig(REIDS_HOST_CONFIG,list);
        LOGGER.info("删除redisHost成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    /**
     * 查询监控主机列表
     * @param vo
     * @return
     */
    public Response<RedisDomain> queryRedisHostList(PageVo vo){
        LOGGER.info("查询redisHost,入参是 page={}<<<<<<<<<<<<<<<<<<<<<<<<<<",vo);
        List<RedisDomain> list = configManager.getConfigObj(REIDS_HOST_CONFIG);
        if(null == list){
            list = new ArrayList<RedisDomain>();
        }
        Response<RedisDomain> rsp = PageUtil.page(list,vo);
        LOGGER.info("查询redisHost成功,出参是 rsp={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",rsp);
        return rsp;
    }

    public Response addAlerms(String json){
        LOGGER.info("添加Alerm,入参是 json={}<<<<<<<<<<<<<<<<<<<<<<<<<<",json);
        List<AlermItem> list = JacksonUtil.toList(json, AlermItem.class);
        if(!CollectionUtils.isEmpty(list)){
            for(AlermItem item : list){
                try{
                    alermManager.addAlerm(item);
                }catch (Exception e){
                    LOGGER.error("添加Alerm:{}失败",item.getName());
                }
            }
        }
        LOGGER.info("添加Alerm,成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        return new Response();
    }

    /**
     * 查询可预警项
     * @param vo
     * @return
     */
    public Response<AlermItem> queryAlermList(PageVo vo){
        LOGGER.info("查询AlermList,入参是 page={}<<<<<<<<<<<<<<<<<<<<<<<<<<",vo);
        Collection<AlermItem> list = alermManager.queryAlermList();
        List<AlermItem> collection = new ArrayList<AlermItem>();
        if(null != list){
            for(AlermItem it : list){
                collection.add(it);
            }
        }
        Response<AlermItem> rsp = PageUtil.page(collection,vo);
        LOGGER.info("查询AlermList成功,出参是 rsp={}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",rsp);
        return rsp;
    }
}
