/*
 * Created on :2016年4月13日
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.cached.redis;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.util.ArrayUtil;
import cn.wuxia.common.util.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.*;

/**
 * 简单的操作，复杂的操作需要直接操作jedis
 * songlin.li
 */
public class RedisClusterCacheClient implements CacheClient {
    private static Logger logger = LoggerFactory.getLogger(RedisClusterCacheClient.class);

    private JedisCluster jedisCluster;

    private int expiredTime = 0;

    private String password;


    @Override
    public void init(String[] servers) {
        if (ArrayUtils.isEmpty(servers)) {
            logger.error("初始化失败，没找到redis服务端");
            return;
        }

        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for (String server : servers) {
            String host = StringUtil.substringBefore(server, ":");
            String port = StringUtil.substringAfter(server, ":");
            HostAndPort hostAndPort = new HostAndPort(host, Integer.parseInt(port));
            nodes.add(hostAndPort);
        }


        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        if (StringUtil.isNotBlank(password)) {
            jedisCluster = new JedisCluster(nodes, 10000, 2000, 5, password, config);
        } else {
            jedisCluster = new JedisCluster(nodes, 10000, config);
        }
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean containKey(String key, String namespace) {
        return containKey(namespace + ":" + key);
    }

    @Override
    public boolean containKey(String key) {
        key = RedisUtils.formatKey(key);
        return BooleanUtils.toBooleanDefaultIfNull(jedisCluster.exists(key), false);
    }

    @Override
    public void add(String key, Object value, int expiredTime) {
        set(key, value, expiredTime);
    }

    @Override
    public void add(String key, Object value, String namespace) {
        set(namespace + ":" + key, value);
    }

    @Override
    public void add(String key, Object value) {
        set(key, value);

    }

    @Override
    public void set(String key, Object value, int expiredTime) {
        if (value == null) {
            return;
        }
        key = RedisUtils.formatKey(key);
        final byte[] keyf = key.getBytes();
        final byte[] valuef = new ObjectsTranscoder().serialize(value);
        jedisCluster.setex(keyf, expiredTime, valuef);
    }

    @Override
    public void set(String key, Object value, String namespace) {
        set(namespace + ":" + key, value);
    }

    @Override
    public void set(String key, Object value) {
        if (value == null) {
            return;
        }
        key = RedisUtils.formatKey(key);
        final byte[] keyf = key.getBytes();
//        if (value instanceof List) {
//            final byte[] valuef = new ListTranscoder().serialize(value);
//            jedisCluster.set(keyf, valuef);
//        } else {
        final byte[] valuef = new ObjectsTranscoder().serialize(value);
        jedisCluster.set(keyf, valuef);
//        }
    }

    @Override
    public void replace(String key, Object value, int expiredTime) {
        set(key, value, expiredTime);
    }

    @Override
    public void replace(String key, Object value, String namespace) {
        set(namespace + ":" + key, value);
    }

    @Override
    public void replace(String key, Object value) {
        set(key, value);
    }

    @Override
    public <T> T get(String key) {
        key = RedisUtils.formatKey(key);
        byte[] value = jedisCluster.get(key.getBytes());
        return (T) new ObjectsTranscoder().deserialize(value);
    }

    @Override
    public long incr(String key) {
        key = RedisUtils.formatKey(key);
        return jedisCluster.incr(key);
    }

    @Override
    public long incr(String key, String namespace) {
        return incr(namespace + ":" + key);
    }

    @Override
    public long incr(String key, long by) {
        key = RedisUtils.formatKey(key);
        return jedisCluster.incrBy(key, by);
    }

    @Override
    public long incr(String key, long by, long defaultValue) {
        key = RedisUtils.formatKey(key);
        Long r = jedisCluster.incrBy(key, by);
        if (r == null) {
            return defaultValue;
        }
        return r;
    }

    @Override
    public long incr(String key, long by, long defaultValue, String namespace) {
        return incr(namespace + ":" + key, by, defaultValue);
    }

    @Override
    public long decr(String key) {
        key = RedisUtils.formatKey(key);
        return jedisCluster.decr(key);
    }

    @Override
    public long decr(String key, String namespace) {
        return decr(namespace + ":" + key);
    }

    @Override
    public long decr(String key, long by) {
        key = RedisUtils.formatKey(key);
        return jedisCluster.incrBy(key, by);
    }

    @Override
    public long decr(String key, long by, long defaultValue) {
        key = RedisUtils.formatKey(key);
        Long r = jedisCluster.decrBy(key, by);
        if (r == null) {
            return defaultValue;
        }
        return r;
    }

    @Override
    public long decr(String key, long by, long defaultValue, String namespace) {
        return decr(namespace + ":" + key, by, defaultValue);
    }

    @Override
    public void delete(String key) {
        key = RedisUtils.formatKey(key);
        jedisCluster.del(key);
    }

    @Override
    public void flushAll() {

    }

    @Override
    public void flushAll(String[] servers) {
        // TODO Auto-generated method stub
    }

    @Override
    public void shutdown() {
        jedisCluster.shutdown();
    }

    @Override
    public void add(String key, Object value, int expiredTime, String namespace) {
        set(namespace + ":" + key, value, expiredTime);
    }

    @Override
    public void set(String key, Object value, int expiredTime, String namespace) {
        set(namespace + ":" + key, value, expiredTime);

    }

    @Override
    public void replace(String key, Object value, int expiredTime, String namespace) {
        set(namespace + ":" + key, value, expiredTime);

    }

    @Override
    public <T> T get(String key, String namespace) {
        return get(namespace + ":" + key);
    }

    @Override
    public void delete(String key, String namespace) {
        delete(namespace + ":" + key);
    }

    @Override
    public void flush(String namespace) {
        Set<String> set = jedisCluster.hkeys(namespace + "*");
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String keyStr = it.next();
            System.out.println(keyStr);
            jedisCluster.del(keyStr);
        }
    }

}
