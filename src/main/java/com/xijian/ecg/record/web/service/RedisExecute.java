package com.xijian.ecg.record.web.service;

import com.xijian.ecg.record.web.domain.ClientDomain;
import com.xijian.ecg.record.web.domain.RedisDomain;
import com.xijian.ecg.record.web.domain.SlowLogDomain;
import com.xijian.ecg.record.web.life.LifeCycle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Slowlog;

import java.util.*;

/**
 * Created by 邵伟 on 2017/5/18 0018.
 */
public class RedisExecute implements LifeCycle{

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisExecute.class);

    private RedisTemplate<String,String> redisTemplate = new RedisTemplate<String, String>();

    public Map<String,Object> getInfo(){
        LOGGER.info("获取info信息<<<<<<<<<<<<<<<<<<<<<<<");
        Map<String,Object> map = new HashMap<String,Object>();
        String info = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Jedis jedis = (Jedis)redisConnection.getNativeConnection();
                String ret = jedis.info("all");
                return ret;
            }
        });
        String[] lines = info.split("\r\n");
        for(String line : lines){
            if(!line.startsWith("#") && StringUtils.isNotEmpty(line)){
                String[] keys = line.replaceAll("\r\n","").split(":");
                map.put(keys[0],keys[1]);
            }
        }
        LOGGER.info("获取info信息结果是{}>>>>>>>>>>>>>>>>>>>>>>>>>>>",map.toString());
        return map;
    }

    public List<ClientDomain> getClientList(){
        LOGGER.info("获取ClientList信息<<<<<<<<<<<<<<<<<<<<<<<");
        List<ClientDomain> list = new ArrayList<ClientDomain>();
        String clientlist = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Jedis jedis = (Jedis)redisConnection.getNativeConnection();
                String ret = jedis.clientList();
                return ret;
            }
        });
        if(StringUtils.isNotEmpty(clientlist)){
            String[] strs = clientlist.split("\n");
            if(null != strs){
                for(String line : strs){
                    if(StringUtils.isNotEmpty(line)){
                        String[] datas = line.split("\\s+");
                        ClientDomain clientDomain = new ClientDomain();
                        list.add(clientDomain);
                        for(String data : datas){
                            clientDomain.setData(data);
                        }
                    }
                }
            }
        }
        LOGGER.info("获取ClientList信息,结果是{}>>>>>>>>>>>>>>>>>>>>>>>>",list.toString());
        return list;
    }

    public List<SlowLogDomain> getSlowLog(){
        LOGGER.info("获取SlowLog信息<<<<<<<<<<<<<<<<<<<<<<<");
        List<SlowLogDomain> list = new ArrayList<SlowLogDomain>();
        List<Slowlog> slowlog = redisTemplate.execute(new RedisCallback<List<Slowlog>>() {
            @Override
            public List<Slowlog> doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Jedis jedis = (Jedis)redisConnection.getNativeConnection();
                List<Slowlog> list = jedis.slowlogGet();
                return list;
            }
        });
        if(!CollectionUtils.isEmpty(slowlog)){
            for(Slowlog log : slowlog){
                SlowLogDomain domain = new SlowLogDomain();
                BeanUtils.copyProperties(log,domain);
                list.add(domain);
            }
        }
        LOGGER.info("获取SlowLog信息,结果是{}>>>>>>>>>>>>>>>>>>>>>>>>>",list.toString());
        return list;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        JedisConnectionFactory connectionFactory = (JedisConnectionFactory)redisTemplate.getConnectionFactory();
        connectionFactory.destroy();
    }

    public void init(RedisDomain redisDomain){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setPoolConfig(jedisPoolConfig);
        connectionFactory.setUsePool(true);
        connectionFactory.setTimeout(5000);
        connectionFactory.setHostName(redisDomain.getIp());
        connectionFactory.setPort(redisDomain.getPort());
        if(StringUtils.isNotEmpty(redisDomain.getPasswd())){
            connectionFactory.setPassword(redisDomain.getPasswd());
        }
        connectionFactory.afterPropertiesSet();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
    }
}
